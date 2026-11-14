package application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.exception.DuplicateCriterionException;
import domain.exception.InvalidIdentifierException;
import domain.exception.InvalidTextException;
import domain.exception.InvalidWeightException;
import domain.exception.RemovalBlockedException;
import domain.exception.UnknownCriterionException;
import domain.exception.UnknownStudentException;
import domain.rubric.Criterion;
import domain.student.StudentId;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import support.CourseFixture;
import support.TestData;

@DisplayName("CourseSetupService — студенты и рубрикатор")
class CourseSetupServiceTest {

    private final CourseFixture course = CourseFixture.create();

    private final CourseSetupService setup = course.setup();

    @Nested
    @DisplayName("студенты")
    class Students {

        @Test
        @DisplayName("добавленный студент получает номер и виден в списке")
        void addsStudent() {
            StudentView anna = setup.addStudent("Анна Смирнова");
            StudentView boris = setup.addStudent("Борис Петров");

            assertEquals(new StudentId(1), anna.id());
            assertEquals(0, anna.evaluationCount());
            assertEquals(List.of(anna, boris), setup.students());
        }

        @Test
        @DisplayName("некорректное ФИО не сохраняется")
        void invalidNameIsNotStored() {
            assertThrows(InvalidTextException.class, () -> setup.addStudent(" "));
            assertTrue(setup.students().isEmpty());
        }

        @Test
        @DisplayName("студента без оценок можно удалить")
        void removesStudentWithoutEvaluations() {
            StudentView anna = setup.addStudent("Анна");

            assertEquals(anna, setup.removeStudent(anna.id().value()));
            assertTrue(setup.students().isEmpty());
        }

        @Test
        @DisplayName("студента с оценками удалить нельзя — он остаётся на курсе")
        void studentWithEvaluationsStays() {
            course.rubrics().save(TestData.rubric());
            int anna = setup.addStudent("Анна").id().value();
            course.grading().evaluate(anna, 1, 5, "");

            assertThrows(RemovalBlockedException.class, () -> setup.removeStudent(anna));
            assertEquals(1, setup.students().size());
        }

        @Test
        @DisplayName("неизвестный и некорректный номер — ошибки модели")
        void unknownStudent() {
            assertThrows(UnknownStudentException.class, () -> setup.removeStudent(5));
            assertThrows(InvalidIdentifierException.class, () -> setup.removeStudent(0));
        }
    }

    @Nested
    @DisplayName("рубрикатор")
    class RubricScenarios {

        @Test
        @DisplayName("критерий добавляется и сохраняется в рубрикаторе")
        void addsCriterion() {
            Criterion exam = setup.addCriterion("Экзамен", 100, new BigDecimal("0.4"));

            assertEquals(List.of(exam), setup.rubric());
            assertEquals(List.of(exam), course.rubrics().get().criteria());
        }

        @Test
        @DisplayName("нарушение правил рубрикатора не меняет его")
        void invalidCriterionIsRejected() {
            setup.addCriterion("Экзамен", 100, BigDecimal.ONE);

            assertThrows(DuplicateCriterionException.class,
                    () -> setup.addCriterion("ЭКЗАМЕН", 10, BigDecimal.ONE));
            assertThrows(InvalidWeightException.class,
                    () -> setup.addCriterion("Проект", 10, BigDecimal.ZERO));
            assertEquals(1, setup.rubric().size());
        }

        @Test
        @DisplayName("критерий без оценок удаляется")
        void removesUnusedCriterion() {
            Criterion exam = setup.addCriterion("Экзамен", 100, BigDecimal.ONE);

            assertEquals(exam, setup.removeCriterion(exam.getId().value()));
            assertTrue(setup.rubric().isEmpty());
        }

        @Test
        @DisplayName("критерий, по которому есть оценки, удалить нельзя")
        void criterionInUseStays() {
            Criterion exam = setup.addCriterion("Экзамен", 100, BigDecimal.ONE);
            setup.addStudent("Анна");
            int boris = setup.addStudent("Борис").id().value();
            course.grading().evaluate(boris, exam.getId().value(), 70, "");

            RemovalBlockedException error = assertThrows(RemovalBlockedException.class,
                    () -> setup.removeCriterion(exam.getId().value()));
            assertTrue(error.getMessage().contains("Экзамен"));
            assertEquals(List.of(exam), setup.rubric());
        }

        @Test
        @DisplayName("неизвестный критерий — ошибка")
        void unknownCriterion() {
            assertThrows(UnknownCriterionException.class, () -> setup.removeCriterion(3));
        }
    }
}

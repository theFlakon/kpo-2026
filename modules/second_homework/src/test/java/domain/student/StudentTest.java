package domain.student;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.exception.DuplicateEvaluationException;
import domain.exception.InvalidTextException;
import domain.exception.MissingEvaluationException;
import domain.exception.RemovalBlockedException;
import domain.rubric.Rubric;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import support.TestData;

@DisplayName("Student — корень агрегата с оценками")
class StudentTest {

    private final Rubric rubric = TestData.rubric();

    private final EvaluationFactory factory = TestData.factory();

    private final Student student = TestData.student(1);

    private Evaluation homework(int points) {
        return factory.create(rubric, TestData.HOMEWORK, points, "");
    }

    @Nested
    @DisplayName("создание")
    class Creation {

        @Test
        @DisplayName("ФИО обрезается и не может быть пустым или длинным")
        void validatesName() {
            assertEquals("Анна", new Student(new StudentId(1), " Анна ").getFullName());
            assertThrows(InvalidTextException.class,
                    () -> new Student(new StudentId(1), ""));
            assertThrows(InvalidTextException.class,
                    () -> new Student(new StudentId(1),
                            "Я".repeat(Student.MAX_NAME_LENGTH + 1)));
        }

        @Test
        @DisplayName("сущность: равенство по номеру, а не по ФИО")
        void equalityById() {
            Student anna = new Student(new StudentId(1), "Анна");

            assertEquals(anna, new Student(new StudentId(1), "Анна Смирнова"));
            assertNotEquals(anna, new Student(new StudentId(2), "Анна"));
        }
    }

    @Nested
    @DisplayName("выставление")
    class Record {

        @Test
        @DisplayName("оценка сохраняется и находится по критерию")
        void recordsEvaluation() {
            Evaluation evaluation = homework(8);

            student.record(evaluation);

            assertEquals(Optional.of(evaluation),
                    student.evaluation(TestData.HOMEWORK));
            assertTrue(student.hasEvaluationFor(TestData.HOMEWORK));
            assertFalse(student.hasEvaluationFor(TestData.EXAM));
        }

        @Test
        @DisplayName("по критерию не больше одной оценки — прежняя не меняется")
        void rejectsSecondEvaluation() {
            student.record(homework(8));

            assertThrows(DuplicateEvaluationException.class,
                    () -> student.record(homework(3)));
            assertEquals(8, student.evaluation(TestData.HOMEWORK).orElseThrow()
                    .getScore().points());
        }

        @Test
        @DisplayName("список оценок нельзя изменить в обход корня")
        void evaluationsAreUnmodifiable() {
            student.record(homework(8));

            assertThrows(UnsupportedOperationException.class,
                    () -> student.evaluations().clear());
            assertEquals(1, student.evaluations().size());
        }
    }

    @Nested
    @DisplayName("исправление и отмена")
    class CorrectAndCancel {

        @Test
        @DisplayName("исправление заменяет оценку и возвращает прежнюю")
        void replaceSwapsEvaluation() {
            Evaluation original = homework(6);
            student.record(original);

            Evaluation previous = student.replace(homework(10));

            assertEquals(original, previous);
            assertEquals(List.of(homework(10)), student.evaluations());
        }

        @Test
        @DisplayName("исправлять нечего — ошибка")
        void replaceRequiresExistingEvaluation() {
            assertThrows(MissingEvaluationException.class,
                    () -> student.replace(homework(10)));
            assertTrue(student.evaluations().isEmpty());
        }

        @Test
        @DisplayName("отмена удаляет оценку; повторная отмена — ошибка")
        void cancelRemovesEvaluation() {
            Evaluation evaluation = homework(6);
            student.record(evaluation);

            assertEquals(evaluation, student.cancel(TestData.HOMEWORK));
            assertTrue(student.evaluation(TestData.HOMEWORK).isEmpty());
            assertThrows(MissingEvaluationException.class,
                    () -> student.cancel(TestData.HOMEWORK));
        }
    }

    @Test
    @DisplayName("студента с оценками нельзя удалить, без оценок — можно")
    void removalRequiresNoEvaluations() {
        assertDoesNotThrow(student::ensureRemovable);

        student.record(homework(5));

        assertThrows(RemovalBlockedException.class, student::ensureRemovable);
    }
}

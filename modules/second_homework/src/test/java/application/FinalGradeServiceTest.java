package application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import adapter.out.persistence.InMemoryRubricRepository;
import adapter.out.persistence.InMemoryStudentRepository;
import domain.exception.EmptyRubricException;
import domain.grading.FinalGradeCalculator;
import domain.grading.FullRubricPolicy;
import domain.grading.GradedOnlyPolicy;
import domain.grading.GradingPolicy;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import support.CourseFixture;
import support.RecordingExporter;
import support.TestPolicies;

@DisplayName("FinalGradeService — итоги, ведомость, экспорт")
class FinalGradeServiceTest {

    private final CourseFixture course = CourseFixture.withRubric();

    private final FinalGradeService finals = course.finals();

    private int student(String name, int homework, int test, Integer exam) {
        int id = course.setup().addStudent(name).id().value();
        course.grading().evaluate(id, 1, homework, "");
        course.grading().evaluate(id, 2, test, "");
        if (exam != null) {
            course.grading().evaluate(id, 3, exam, "");
        }
        return id;
    }

    @Nested
    @DisplayName("правила итога")
    class Policies {

        @Test
        @DisplayName("правила в порядке конфигурации; первое — официальное")
        void policiesInConfiguredOrder() {
            assertEquals(List.of(FullRubricPolicy.CODE, GradedOnlyPolicy.CODE),
                    finals.policies().stream().map(GradingPolicy::code).toList());
        }

        @Test
        @DisplayName("неизвестный код правила — ошибка")
        void unknownPolicy() {
            assertThrows(UnknownPolicyException.class, () -> finals.sheet("median"));
        }

        @Test
        @DisplayName("пустой список и повтор кода — ошибки конфигурации")
        void invalidConfiguration() {
            List<GradingPolicy> duplicates = List.of(TestPolicies.fixed("a", "1"),
                    TestPolicies.fixed("a", "2"));

            assertThrows(IllegalArgumentException.class, () -> service(List.of()));
            assertThrows(IllegalArgumentException.class, () -> service(duplicates));
        }

        private FinalGradeService service(List<GradingPolicy> policies) {
            return new FinalGradeService(new InMemoryStudentRepository(),
                    new InMemoryRubricRepository(), new FinalGradeCalculator(),
                    policies, new RecordingExporter());
        }
    }

    @Nested
    @DisplayName("итоги")
    class Grades {

        @Test
        @DisplayName("итог студента зависит от выбранного правила")
        void studentGradeByPolicy() {
            int vera = student("Вера", 4, 8, null);

            assertEquals(new BigDecimal("2"),
                    finals.finalGrade(vera, FullRubricPolicy.CODE).grade().value());
            assertEquals(new BigDecimal("4.00"),
                    finals.finalGrade(vera, GradedOnlyPolicy.CODE).grade().value());
        }

        @Test
        @DisplayName("ведомость: все студенты курса и аналитика")
        void sheetWithAnalytics() {
            student("Анна", 9, 18, 85);
            student("Борис", 6, 11, 52);
            student("Вера", 4, 8, null);

            GradeSheet sheet = finals.sheet(FullRubricPolicy.CODE);

            assertEquals(List.of("Анна", "Борис", "Вера"), sheet.grades().stream()
                    .map(line -> line.student().fullName()).toList());
            assertEquals(List.of("9", "6", "2"), sheet.grades().stream()
                    .map(line -> line.grade().value().toPlainString()).toList());
            assertEquals(new BigDecimal("5.67"), sheet.average().orElseThrow());
            assertEquals(1, sheet.failingCount());
            assertEquals(1, sheet.incompleteCount());
            assertEquals(FullRubricPolicy.CODE, sheet.policyCode());
        }

        @Test
        @DisplayName("без критериев итог не считается")
        void emptyRubric() {
            CourseFixture empty = CourseFixture.create();
            int anna = empty.setup().addStudent("Анна").id().value();

            assertThrows(EmptyRubricException.class,
                    () -> empty.finals().finalGrade(anna, FullRubricPolicy.CODE));
        }
    }

    @Test
    @DisplayName("экспорт передаёт в порт ведомость по правилу и место выгрузки")
    void exportGoesThroughPort() {
        student("Анна", 9, 18, 85);

        String result = finals.export(GradedOnlyPolicy.CODE, "итоги.csv");

        assertEquals(RecordingExporter.PREFIX + "итоги.csv", result);
        assertEquals(List.of("итоги.csv"), course.exporter().destinations());
        GradeSheet exported = course.exporter().sheets().get(0);
        assertEquals(GradedOnlyPolicy.CODE, exported.policyCode());
        assertEquals(1, exported.grades().size());
    }

    @Test
    @DisplayName("экспорт с неизвестным правилом до порта не доходит")
    void exportWithUnknownPolicy() {
        assertThrows(UnknownPolicyException.class, () -> finals.export("x", "f.csv"));
        assertTrue(course.exporter().sheets().isEmpty());
    }
}

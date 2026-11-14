package support;

import adapter.out.persistence.InMemoryRubricRepository;
import adapter.out.persistence.InMemoryStudentRepository;
import application.CourseSetupService;
import application.FinalGradeService;
import application.GradingService;
import domain.grading.FinalGradeCalculator;
import domain.rubric.RubricRepository;
import domain.student.StudentRepository;

/**
 * Собранный курс для тестов сценариев: сервисы поверх свежих хранилищ.
 *
 * <p>Собирается как в приложении, но с фиксированными часами и подменной
 * выгрузкой. Каждый вызов {@link #create()} создаёт новый курс, поэтому
 * состояние между тестами не протекает.
 *
 * @param students хранилище студентов
 * @param rubrics  хранилище рубрикатора
 * @param setup    сценарии «студенты и рубрикатор»
 * @param grading  сценарии «оценки»
 * @param finals   сценарии «итоги»
 * @param exporter подменная выгрузка
 */
public record CourseFixture(StudentRepository students,
                            RubricRepository rubrics,
                            CourseSetupService setup,
                            GradingService grading,
                            FinalGradeService finals,
                            RecordingExporter exporter) {

    /**
     * Создаёт пустой курс.
     *
     * @return новый курс
     */
    public static CourseFixture create() {
        StudentRepository students = new InMemoryStudentRepository();
        RubricRepository rubrics = new InMemoryRubricRepository();
        FinalGradeCalculator calculator = new FinalGradeCalculator();
        RecordingExporter exporter = new RecordingExporter();
        return new CourseFixture(students, rubrics,
                new CourseSetupService(students, rubrics),
                new GradingService(students, rubrics, TestData.factory(),
                        calculator),
                new FinalGradeService(students, rubrics, calculator,
                        TestPolicies.configured(), exporter),
                exporter);
    }

    /**
     * Создаёт курс с рубрикатором {@link TestData#rubric()}.
     *
     * @return новый курс с тремя критериями и без студентов
     */
    public static CourseFixture withRubric() {
        CourseFixture course = create();
        course.rubrics().save(TestData.rubric());
        return course;
    }
}

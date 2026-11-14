package application;

import domain.grading.FinalGradeCalculator;
import domain.grading.GradingPolicy;
import domain.rubric.Rubric;
import domain.rubric.RubricRepository;
import domain.student.Student;
import domain.student.StudentId;
import domain.student.StudentRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

/**
 * Сценарии «итоги»: итог студента, ведомость с аналитикой, экспорт.
 *
 * <p>Правила итога приходят списком из конфигурации: пользователь
 * выбирает правило при каждом запросе, а сервис о конкретных правилах
 * не знает.
 */
public final class FinalGradeService {

    /** Хранилище студентов. */
    private final StudentRepository students;

    /** Хранилище рубрикатора. */
    private final RubricRepository rubrics;

    /** Расчёт итога. */
    private final FinalGradeCalculator calculator;

    /** Правила итога по коду в порядке конфигурации. */
    private final Map<String, GradingPolicy> policies =
            new LinkedHashMap<>();

    /** Выгрузка ведомости. */
    private final GradeSheetExporter exporter;

    /**
     * Создаёт сервис.
     *
     * @param students   хранилище студентов
     * @param rubrics    хранилище рубрикатора
     * @param calculator расчёт итога
     * @param policies   непустой список правил с уникальными кодами
     * @param exporter   выгрузка ведомости
     * @throws IllegalArgumentException если правил нет или коды
     *                                  повторяются — ошибка конфигурации
     */
    public FinalGradeService(@NonNull StudentRepository students,
                             @NonNull RubricRepository rubrics,
                             @NonNull FinalGradeCalculator calculator,
                             @NonNull List<GradingPolicy> policies,
                             @NonNull GradeSheetExporter exporter) {
        if (policies.isEmpty()) {
            throw new IllegalArgumentException("Не задано ни одного правила");
        }
        for (GradingPolicy policy : policies) {
            if (this.policies.putIfAbsent(policy.code(), policy) != null) {
                throw new IllegalArgumentException(
                        "Повторяется код правила: " + policy.code());
            }
        }
        this.students = students;
        this.rubrics = rubrics;
        this.calculator = calculator;
        this.exporter = exporter;
    }

    /**
     * Возвращает доступные правила итога.
     *
     * @return неизменяемый список в порядке конфигурации; первое правило —
     *         официальное
     */
    public List<GradingPolicy> policies() {
        return List.copyOf(policies.values());
    }

    /**
     * Считает итог одного студента.
     *
     * @param studentId  номер студента
     * @param policyCode код правила итога
     * @return итог студента
     * @throws domain.exception.PosException если нет студента или правила
     *         либо рубрикатор пуст
     */
    public StudentGrade finalGrade(int studentId, String policyCode) {
        GradingPolicy policy = policy(policyCode);
        Student student = students.get(new StudentId(studentId));
        return new StudentGrade(StudentView.of(student),
                calculator.calculate(rubrics.get(), student, policy));
    }

    /**
     * Строит итоговую ведомость всего курса.
     *
     * @param policyCode код правила итога
     * @return ведомость с аналитикой
     * @throws domain.exception.PosException если нет правила
     *         или рубрикатор пуст
     */
    public GradeSheet sheet(String policyCode) {
        GradingPolicy policy = policy(policyCode);
        Rubric rubric = rubrics.get();
        List<StudentGrade> grades = students.findAll().stream()
                .map(student -> new StudentGrade(StudentView.of(student),
                        calculator.calculate(rubric, student, policy)))
                .toList();
        return new GradeSheet(policy.code(), policy.description(), grades);
    }

    /**
     * Выгружает итоговую ведомость через порт экспорта.
     *
     * @param policyCode  код правила итога
     * @param destination куда выгрузить; смысл определяет адаптер
     * @return описание результата, например путь к файлу
     * @throws domain.exception.PosException если нет правила, рубрикатор
     *         пуст или выгрузка не удалась
     */
    public String export(String policyCode, String destination) {
        return exporter.export(sheet(policyCode), destination);
    }

    /**
     * Находит правило итога по коду.
     *
     * @param code код правила
     * @return правило
     * @throws UnknownPolicyException если правила нет
     */
    private GradingPolicy policy(String code) {
        GradingPolicy policy = policies.get(code);
        if (policy == null) {
            throw new UnknownPolicyException(code);
        }
        return policy;
    }
}

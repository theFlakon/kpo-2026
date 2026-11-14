package adapter.in.console;

import application.CourseSetupService;
import application.FinalGradeService;
import application.GradingService;
import application.StudentView;
import domain.exception.PosException;
import domain.grading.GradingPolicy;
import domain.rubric.Criterion;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Сквозной сценарий без участия пользователя: студенты и критерии →
 * оценки → исправление и отмена → итоги и аналитика → экспорт.
 *
 * <p>Попутно показывает, что модель не даёт нарушить свои правила:
 * каждое нарушение — отдельный шаг с сообщением об ошибке.
 *
 * <p>Работает с тем же курсом, что и меню, и запускается повторно без
 * ошибок: студенты добавляются заново, а критерии с демонстрационными
 * названиями переиспользуются, если уже есть.
 */
@RequiredArgsConstructor
public final class DemoScenario {

    /** Студенты демонстрации. */
    private static final String[] STUDENTS = {
        "Анна Смирнова", "Борис Петров", "Вера Козлова",
    };

    /** Названия критериев демонстрации. */
    private static final String[] CRITERIA = {
        "Домашние задания", "Контрольная работа", "Экзамен",
    };

    /** Максимумы критериев: разные, чтобы показать нормировку. */
    private static final int[] MAX_SCORES = {10, 20, 100};

    /** Веса критериев. */
    private static final String[] WEIGHTS = {"0.3", "0.3", "0.4"};

    /** Баллы: строка — студент, столбец — критерий. */
    private static final int[][] SCORES = {
        {9, 18, 85},
        {6, 11, 52},
        {4, 8, 30},
    };

    /** Индекс критерия «Домашние задания». */
    private static final int HOMEWORK = 0;

    /** Индекс критерия «Экзамен». */
    private static final int EXAM = 2;

    /** Балл больше максимума домашних заданий. */
    private static final int TOO_HIGH_POINTS = 12;

    /** Номер критерия, которого нет в рубрикаторе. */
    private static final int UNKNOWN_CRITERION = 999;

    /** Исправленный балл за домашние задания. */
    private static final int CORRECTED_POINTS = 10;

    /** Файл выгрузки демонстрации. */
    private static final String EXPORT_FILE = "demo-final-grades.csv";

    /** Сценарии «студенты и рубрикатор». */
    @NonNull
    private final CourseSetupService setup;

    /** Сценарии «оценки». */
    @NonNull
    private final GradingService grading;

    /** Сценарии «итоги». */
    @NonNull
    private final FinalGradeService finals;

    /** Форматирование вывода. */
    @NonNull
    private final ConsolePrinter printer;

    /**
     * Прогоняет сквозной сценарий и печатает результат.
     */
    public void run() {
        List<Integer> students = addStudents();
        List<Criterion> criteria = prepareRubric();
        evaluate(students, criteria);
        correctAndCancel(students, criteria);
        showFinals();

        printer.printSection("Демонстрация: экспорт итога");
        String official = finals.policies().get(0).code();
        attempt("выгрузка официального итога в CSV",
                () -> printer.printExported(
                        finals.export(official, EXPORT_FILE)));
    }

    /**
     * Добавляет студентов демонстрации.
     *
     * @return номера добавленных студентов
     */
    private List<Integer> addStudents() {
        printer.printSection("Демонстрация: студенты");
        List<Integer> ids = new ArrayList<>();
        for (String name : STUDENTS) {
            StudentView student = setup.addStudent(name);
            printer.printStudent("Добавлен студент", student);
            ids.add(student.id().value());
        }
        attempt("студент с пустым ФИО", () -> setup.addStudent("   "));
        return ids;
    }

    /**
     * Готовит рубрикатор: добавляет недостающие критерии демонстрации.
     *
     * @return критерии демонстрации в порядке {@link #CRITERIA}
     */
    private List<Criterion> prepareRubric() {
        printer.printSection("Демонстрация: рубрикатор");
        List<Criterion> criteria = new ArrayList<>();
        for (int i = 0; i < CRITERIA.length; i++) {
            criteria.add(findOrAdd(CRITERIA[i], MAX_SCORES[i],
                    new BigDecimal(WEIGHTS[i])));
        }
        attempt("критерий с уже занятым названием (регистр не важен)",
                () -> setup.addCriterion(
                        CRITERIA[EXAM].toLowerCase(Locale.ROOT),
                        MAX_SCORES[EXAM], BigDecimal.ONE));
        attempt("критерий с максимумом 0",
                () -> setup.addCriterion("Устный опрос", 0, BigDecimal.ONE));
        attempt("критерий с отрицательным весом",
                () -> setup.addCriterion("Устный опрос",
                        MAX_SCORES[HOMEWORK], BigDecimal.ONE.negate()));
        printer.printRubric(setup.rubric());
        return criteria;
    }

    /**
     * Выставляет оценки и показывает нарушения правил.
     *
     * @param students номера студентов
     * @param criteria критерии
     */
    private void evaluate(List<Integer> students, List<Criterion> criteria) {
        printer.printSection("Демонстрация: оценки");
        for (int s = 0; s < students.size(); s++) {
            for (int c = 0; c < criteria.size(); c++) {
                int student = students.get(s);
                int criterion = id(criteria.get(c));
                int points = SCORES[s][c];
                attempt("оценка студенту №" + student,
                        () -> printer.printGradeLine("Выставлено",
                                grading.evaluate(student, criterion, points,
                                        "")));
            }
        }
        int first = students.get(0);
        int homework = id(criteria.get(HOMEWORK));
        attempt("балл " + TOO_HIGH_POINTS + " при максимуме "
                        + MAX_SCORES[HOMEWORK],
                () -> grading.evaluate(first, homework, TOO_HIGH_POINTS, ""));
        attempt("оценка по критерию, которого нет в рубрикаторе",
                () -> grading.evaluate(first, UNKNOWN_CRITERION, 1, ""));
        attempt("вторая оценка по тому же критерию",
                () -> grading.evaluate(first, homework, 1, ""));
        printer.printCard(grading.card(first));
    }

    /**
     * Исправляет и отменяет оценки, показывая, что итог пересчитывается.
     *
     * @param students номера студентов
     * @param criteria критерии
     */
    private void correctAndCancel(List<Integer> students,
                                  List<Criterion> criteria) {
        printer.printSection("Демонстрация: исправление и отмена");
        GradingPolicy official = finals.policies().get(0);
        int second = students.get(1);
        int third = students.get(2);

        printer.printStep("итог до исправления");
        printer.printFinalGrade(finals.finalGrade(second, official.code()),
                official.description());
        attempt("исправление оценки за домашние задания",
                () -> printer.printGradeLine("Исправлено",
                        grading.correct(second, id(criteria.get(HOMEWORK)),
                                CORRECTED_POINTS,
                                "Пересдал после консультации")));
        printer.printStep("итог после исправления — пересчитан сам");
        printer.printFinalGrade(finals.finalGrade(second, official.code()),
                official.description());

        attempt("отмена оценки за экзамен",
                () -> printer.printGradeLine("Отменено",
                        grading.cancel(third, id(criteria.get(EXAM)))));
        attempt("повторная отмена той же оценки",
                () -> grading.cancel(third, id(criteria.get(EXAM))));
        attempt("удаление студента с оценками",
                () -> setup.removeStudent(third));
        printer.printCard(grading.card(third));
    }

    /**
     * Печатает итоговую ведомость по каждому правилу итога.
     */
    private void showFinals() {
        printer.printSection("Демонстрация: итоги и аналитика");
        for (GradingPolicy policy : finals.policies()) {
            printer.printSheet(finals.sheet(policy.code()));
        }
    }

    /**
     * Находит критерий по названию или добавляет его.
     *
     * @param name     название
     * @param maxScore максимальный балл
     * @param weight   вес
     * @return критерий рубрикатора
     */
    private Criterion findOrAdd(String name, int maxScore, BigDecimal weight) {
        // Повторный запуск демонстрации не должен падать на уже
        // добавленных критериях: переиспользуем их.
        return setup.rubric().stream()
                .filter(criterion -> criterion.getName().equals(name))
                .findFirst()
                .orElseGet(() -> {
                    Criterion added = setup.addCriterion(name, maxScore,
                            weight);
                    printer.printCriterion("Добавлен критерий", added);
                    return added;
                });
    }

    /**
     * Номер критерия.
     *
     * @param criterion критерий
     * @return номер
     */
    private static int id(Criterion criterion) {
        return criterion.getId().value();
    }

    /**
     * Выполняет шаг и печатает ошибку модели, если она есть.
     *
     * @param description что делает шаг
     * @param step        действие шага
     */
    private void attempt(String description, Runnable step) {
        printer.printStep(description);
        try {
            step.run();
        } catch (PosException e) {
            printer.printError(e);
        }
    }
}

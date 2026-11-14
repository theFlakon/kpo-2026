package adapter.in.console;

import application.CourseSetupService;
import application.FinalGradeService;
import application.GradingService;
import application.StudentView;
import domain.exception.PosException;
import domain.grading.GradeLine;
import domain.grading.GradingPolicy;
import domain.rubric.Criterion;
import domain.rubric.Score;
import java.math.BigDecimal;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Диалог с пользователем через текстовое меню.
 *
 * <p>Ошибки модели ({@link PosException}) перехватываются на уровне
 * пункта меню: пользователь видит сообщение и возвращается в меню.
 * Любое другое исключение означает ошибку в коде и не скрывается.
 */
@RequiredArgsConstructor
public final class ConsoleApp {

    /** Имя файла выгрузки по умолчанию. */
    static final String DEFAULT_EXPORT_FILE = "final-grades.csv";

    /** Подсказка для оценки без комментария. */
    private static final String COMMENT_PROMPT =
            "Комментарий (Enter — без комментария)";

    /** Сценарии «студенты и рубрикатор». */
    @NonNull
    private final CourseSetupService setup;

    /** Сценарии «оценки». */
    @NonNull
    private final GradingService grading;

    /** Сценарии «итоги». */
    @NonNull
    private final FinalGradeService finals;

    /** Чтение ввода. */
    @NonNull
    private final ConsoleInput input;

    /** Форматирование вывода. */
    @NonNull
    private final ConsolePrinter printer;

    /** Сквозной сценарий. */
    @NonNull
    private final DemoScenario demo;

    /**
     * Запускает меню и работает до выбора «Выход» или конца ввода.
     */
    public void run() {
        printer.printWelcome();
        try {
            boolean running = true;
            while (running) {
                printer.printMenu();
                running = handle(input.readLine("Выберите пункт"));
            }
        } catch (InputClosedException e) {
            printer.printSection("ввод завершён");
        }
        printer.printGoodbye();
    }

    /**
     * Выполняет пункт меню.
     *
     * @param choice введённый номер пункта
     * @return {@code false}, если пользователь выбрал выход
     */
    private boolean handle(String choice) {
        // switch по пунктам меню — сознательно ограниченный OCP: новый
        // пункт требует правки этого метода и текста меню. Паттерн
        // «Команда» для полутора десятков пунктов был бы overengineering,
        // а консоль — самое дешёвое для правки место.
        try {
            switch (choice) {
                case "1" -> printer.printStudent("Добавлен студент",
                        setup.addStudent(input.readLine("ФИО студента")));
                case "2" -> printer.printStudents(setup.students());
                case "3" -> printer.printStudent("Удалён студент",
                        setup.removeStudent(chooseStudent()));
                case "4" -> addCriterion();
                case "5" -> printer.printRubric(setup.rubric());
                case "6" -> printer.printCriterion("Удалён критерий",
                        setup.removeCriterion(chooseCriterion().getId()
                                .value()));
                case "7" -> evaluate();
                case "8" -> correct();
                case "9" -> cancel();
                case "10" -> printer.printCard(grading.card(chooseStudent()));
                case "11" -> finalGrade();
                case "12" -> printer.printSheet(
                        finals.sheet(choosePolicy().code()));
                case "13" -> export();
                case "14" -> demo.run();
                case "0" -> {
                    return false;
                }
                default -> printer.printUnknownCommand(choice);
            }
        } catch (PosException e) {
            printer.printError(e);
        } catch (NothingToChooseException e) {
            printer.printHint(e.getMessage());
        }
        return true;
    }

    /**
     * Добавляет критерий: спрашивает название, максимум и вес.
     */
    private void addCriterion() {
        String name = input.readLine("Название критерия");
        int maxScore = input.readInt("Максимальный балл ("
                + Score.MIN_MAX_POINTS + "–" + Score.MAX_MAX_POINTS + ")");
        BigDecimal weight = input.readDecimal(
                "Вес (доля или проценты, например 0,3 или 30)");
        printer.printCriterion("Добавлен критерий",
                setup.addCriterion(name, maxScore, weight));
    }

    /**
     * Выставляет оценку: студент, критерий, балл, комментарий.
     */
    private void evaluate() {
        int studentId = chooseStudent();
        Criterion criterion = chooseCriterion();
        int points = input.readInt("Балл (0–" + criterion.getMaxScore()
                + ")");
        String comment = input.readLine(COMMENT_PROMPT);
        printer.printGradeLine("Выставлено", grading.evaluate(studentId,
                criterion.getId().value(), points, comment));
    }

    /**
     * Исправляет одну из выставленных оценок студента.
     */
    private void correct() {
        int studentId = chooseStudent();
        GradeLine line = chooseGraded(studentId);
        int points = input.readInt("Новый балл (0–"
                + line.getCriterion().getMaxScore() + ")");
        String comment = input.readLine(COMMENT_PROMPT);
        printer.printGradeLine("Исправлено", grading.correct(studentId,
                line.getCriterion().getId().value(), points, comment));
    }

    /**
     * Отменяет одну из выставленных оценок студента.
     */
    private void cancel() {
        int studentId = chooseStudent();
        GradeLine line = chooseGraded(studentId);
        printer.printGradeLine("Отменено", grading.cancel(studentId,
                line.getCriterion().getId().value()));
    }

    /**
     * Показывает итог одного студента по выбранному правилу.
     */
    private void finalGrade() {
        int studentId = chooseStudent();
        GradingPolicy policy = choosePolicy();
        printer.printFinalGrade(finals.finalGrade(studentId, policy.code()),
                policy.description());
    }

    /**
     * Выгружает итоговую ведомость по выбранному правилу.
     */
    private void export() {
        GradingPolicy policy = choosePolicy();
        String file = input.readLine("Имя файла", DEFAULT_EXPORT_FILE);
        printer.printExported(finals.export(policy.code(), file));
    }

    /**
     * Предлагает выбрать студента.
     *
     * @return номер выбранного студента
     */
    private int chooseStudent() {
        List<StudentView> students = setup.students();
        return input.choose("Студент", students, printer::studentLabel,
                "Сначала добавьте студента (пункт 1)").id().value();
    }

    /**
     * Предлагает выбрать критерий рубрикатора.
     *
     * @return выбранный критерий
     */
    private Criterion chooseCriterion() {
        return input.choose("Критерий", setup.rubric(),
                printer::criterionLabel,
                "Сначала добавьте критерий (пункт 4)");
    }

    /**
     * Предлагает выбрать одну из выставленных оценок студента.
     *
     * @param studentId номер студента
     * @return строка ведомости с оценкой
     */
    private GradeLine chooseGraded(int studentId) {
        List<GradeLine> graded = grading.card(studentId).lines().stream()
                .filter(GradeLine::isGraded)
                .toList();
        return input.choose("Оценка", graded, printer::lineLabel,
                "У студента нет выставленных оценок");
    }

    /**
     * Предлагает выбрать правило итога.
     *
     * @return выбранное правило
     */
    private GradingPolicy choosePolicy() {
        return input.choose("Правило итога", finals.policies(),
                GradingPolicy::description, "Правила итога не настроены");
    }
}

package adapter.in.console;

import application.GradeSheet;
import application.StudentCard;
import application.StudentGrade;
import application.StudentView;
import domain.exception.PosException;
import domain.grading.FinalGrade;
import domain.grading.GradeLine;
import domain.rubric.Criterion;
import domain.student.Evaluation;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Форматирование вывода консоли.
 *
 * <p>Используется и меню, и демонстрацией, поэтому данные печатаются
 * одинаково в обоих сценариях. Сценарии возвращают данные, а строки
 * появляются только здесь.
 */
@RequiredArgsConstructor
public final class ConsolePrinter {

    /** Формат даты оценки. */
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    /** Отметка невыставленной оценки. */
    private static final String NO_GRADE = "—";

    /** Текст главного меню. */
    private static final String MENU = """

            Студенты:
               1. Добавить студента
               2. Список студентов
               3. Удалить студента
            Рубрикатор:
               4. Добавить критерий
               5. Показать рубрикатор
               6. Удалить критерий
            Оценки:
               7. Выставить оценку
               8. Исправить оценку
               9. Отменить оценку
              10. Оценки студента
            Итоги:
              11. Итог студента
              12. Итоговая ведомость и аналитика
              13. Экспорт итогов в CSV
            Прочее:
              14. Демонстрация сквозного сценария
               0. Выход""";

    /** Поток вывода. */
    @NonNull
    private final PrintStream out;

    /**
     * Печатает приветствие.
     */
    public void printWelcome() {
        out.println("=== ПОС — Преподавательская Оценка Студентов ===");
        out.println("«Обратная связь — спорт для двоих»");
    }

    /**
     * Печатает главное меню.
     */
    public void printMenu() {
        out.println(MENU);
    }

    /**
     * Печатает заголовок раздела.
     *
     * @param title текст заголовка
     */
    public void printSection(String title) {
        out.println();
        out.println("--- " + title + " ---");
    }

    /**
     * Печатает описание шага демонстрации.
     *
     * @param description что делает шаг
     */
    public void printStep(String description) {
        out.println("> " + description);
    }

    /**
     * Печатает сообщение об ошибке модели или сценария.
     *
     * @param error перехваченное исключение проекта
     */
    public void printError(@NonNull PosException error) {
        out.println("  Ошибка: " + error.getMessage());
    }

    /**
     * Печатает подсказку пользователю.
     *
     * @param hint текст подсказки
     */
    public void printHint(String hint) {
        out.println("  " + hint);
    }

    /**
     * Печатает сообщение о неизвестной команде меню.
     *
     * @param command введённая команда
     */
    public void printUnknownCommand(String command) {
        out.println("Нет такого пункта меню: '" + command + "'.");
    }

    /**
     * Печатает студента после изменения.
     *
     * @param action  что произошло, например «Добавлен студент»
     * @param student студент
     */
    public void printStudent(String action, @NonNull StudentView student) {
        out.println("  " + action + ": " + studentLabel(student));
    }

    /**
     * Печатает список студентов.
     *
     * @param students студенты курса
     */
    public void printStudents(@NonNull List<StudentView> students) {
        out.println("Студенты курса:");
        if (students.isEmpty()) {
            out.println("  студентов пока нет");
            return;
        }
        students.forEach(student -> out.println("  " + studentLabel(student)
                + ", оценок: " + student.evaluationCount()));
    }

    /**
     * Печатает критерий после изменения.
     *
     * @param action    что произошло, например «Добавлен критерий»
     * @param criterion критерий
     */
    public void printCriterion(String action, @NonNull Criterion criterion) {
        out.println("  " + action + ": " + criterionLabel(criterion));
    }

    /**
     * Печатает рубрикатор.
     *
     * @param criteria критерии рубрикатора
     */
    public void printRubric(@NonNull List<Criterion> criteria) {
        out.println("Рубрикатор (веса нормируются на их сумму):");
        if (criteria.isEmpty()) {
            out.println("  критериев пока нет");
            return;
        }
        criteria.forEach(criterion -> out.println("  "
                + criterionLabel(criterion)));
    }

    /**
     * Печатает строку ведомости после изменения оценки.
     *
     * @param action что произошло, например «Выставлено»
     * @param line   строка с оценкой
     */
    public void printGradeLine(String action, @NonNull GradeLine line) {
        out.println("  " + action + ": " + lineLabel(line));
    }

    /**
     * Печатает оценки студента по всем критериям.
     *
     * @param card ведомость студента
     */
    public void printCard(@NonNull StudentCard card) {
        out.println("Оценки: " + studentLabel(card.student()));
        if (card.lines().isEmpty()) {
            out.println("  в рубрикаторе нет критериев");
            return;
        }
        card.lines().forEach(line -> out.println("  " + lineLabel(line)));
    }

    /**
     * Печатает итог одного студента.
     *
     * @param grade  итог студента
     * @param policy описание правила, по которому он посчитан
     */
    public void printFinalGrade(@NonNull StudentGrade grade, String policy) {
        out.println("Итог: " + studentLabel(grade.student()) + " — "
                + gradeLabel(grade.grade()));
        out.println("  правило: " + policy);
    }

    /**
     * Печатает итоговую ведомость и аналитику.
     *
     * @param sheet итоговая ведомость
     */
    public void printSheet(@NonNull GradeSheet sheet) {
        out.println("Итоговая ведомость — " + sheet.policyDescription()
                + ":");
        if (sheet.grades().isEmpty()) {
            out.println("  студентов пока нет");
            return;
        }
        sheet.grades().forEach(line -> out.println(String.format(
                "  %-36s %s", studentLabel(line.student()),
                gradeLabel(line.grade()))));
        out.println("Аналитика: студентов " + sheet.grades().size()
                + ", средний итог " + number(sheet.average())
                + ", лучший " + number(sheet.best())
                + ", худший " + number(sheet.worst())
                + ", неудовлетворительно " + sheet.failingCount()
                + ", неполных ведомостей " + sheet.incompleteCount());
    }

    /**
     * Печатает результат выгрузки.
     *
     * @param location где лежит выгрузка
     */
    public void printExported(String location) {
        out.println("  Ведомость выгружена: " + location);
    }

    /**
     * Печатает прощание.
     */
    public void printGoodbye() {
        out.println("До встречи! Обратная связь — спорт для двоих.");
    }

    /**
     * Подпись студента: «№3 Вера Козлова».
     *
     * @param student студент
     * @return подпись
     */
    public String studentLabel(@NonNull StudentView student) {
        return "№" + student.id().value() + " " + student.fullName();
    }

    /**
     * Подпись критерия: «№1 Экзамен (макс. 100, вес 0,4)».
     *
     * @param criterion критерий
     * @return подпись
     */
    public String criterionLabel(@NonNull Criterion criterion) {
        return "№" + criterion.getId().value() + " " + criterion.getName()
                + " (макс. " + criterion.getMaxScore() + ", вес "
                + number(criterion.getWeight().value()) + ")";
    }

    /**
     * Подпись строки ведомости: «Экзамен: 85 из 100, 12.11.2026».
     *
     * @param line строка ведомости
     * @return подпись
     */
    public String lineLabel(@NonNull GradeLine line) {
        String grade = line.getEvaluation()
                .map(ConsolePrinter::evaluationLabel)
                .orElse(NO_GRADE);
        return line.getCriterion().getName() + ": " + grade;
    }

    /**
     * Подпись оценки: «85 из 100, 12.11.2026, «комментарий»».
     *
     * @param evaluation оценка
     * @return подпись
     */
    private static String evaluationLabel(Evaluation evaluation) {
        String text = evaluation.getScore().points() + " из "
                + evaluation.getScore().maxPoints() + ", "
                + DATE.format(evaluation.getDate());
        return evaluation.getComment()
                .map(comment -> text + ", «" + comment + "»")
                .orElse(text);
    }

    /**
     * Подпись итога: «7 (оценено 2 из 3, неуд.)».
     *
     * @param grade итог
     * @return подпись
     */
    private static String gradeLabel(FinalGrade grade) {
        String text = number(grade.value()) + " (оценено "
                + grade.gradedCriteria() + " из " + grade.totalCriteria();
        return grade.isPassing() ? text + ")" : text + ", неуд.)";
    }

    /**
     * Записывает число с десятичной запятой.
     *
     * @param value число
     * @return запись вида «7,45»
     */
    private static String number(BigDecimal value) {
        return value.toPlainString().replace('.', ',');
    }

    /**
     * Записывает необязательное число.
     *
     * @param value число или пустой {@code Optional}
     * @return запись числа или прочерк
     */
    private static String number(Optional<BigDecimal> value) {
        return value.map(ConsolePrinter::number).orElse(NO_GRADE);
    }
}

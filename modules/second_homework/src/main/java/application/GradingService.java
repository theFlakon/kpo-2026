package application;

import domain.grading.FinalGradeCalculator;
import domain.grading.GradeLine;
import domain.rubric.CriterionId;
import domain.rubric.Rubric;
import domain.rubric.RubricRepository;
import domain.student.Evaluation;
import domain.student.EvaluationFactory;
import domain.student.Student;
import domain.student.StudentId;
import domain.student.StudentRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Сценарии «оценки»: выставить, исправить, отменить, посмотреть.
 *
 * <p>Каждый сценарий изменения — загрузить студента и рубрикатор,
 * создать оценку фабрикой, передать её корню агрегата, сохранить.
 * Все проверки — в модели; сервис только связывает шаги.
 */
@RequiredArgsConstructor
public final class GradingService {

    /** Хранилище студентов. */
    @NonNull
    private final StudentRepository students;

    /** Хранилище рубрикатора. */
    @NonNull
    private final RubricRepository rubrics;

    /** Создание оценок с проверкой по рубрикатору. */
    @NonNull
    private final EvaluationFactory evaluations;

    /** Сборка ведомости студента. */
    @NonNull
    private final FinalGradeCalculator calculator;

    /**
     * Выставляет оценку.
     *
     * @param studentId   номер студента
     * @param criterionId номер критерия
     * @param points      балл
     * @param comment     комментарий, может быть пустым
     * @return строка ведомости с новой оценкой
     * @throws domain.exception.PosException если нарушено правило модели:
     *         нет студента или критерия, балл вне пределов, оценка уже есть
     */
    public GradeLine evaluate(int studentId, int criterionId, int points,
                              String comment) {
        Rubric rubric = rubrics.get();
        Student student = students.get(new StudentId(studentId));
        Evaluation evaluation = evaluations.create(rubric,
                new CriterionId(criterionId), points, comment);
        student.record(evaluation);
        students.save(student);
        return line(rubric, evaluation);
    }

    /**
     * Исправляет оценку: новая оценка проходит те же проверки.
     *
     * @param studentId   номер студента
     * @param criterionId номер критерия
     * @param points      новый балл
     * @param comment     новый комментарий, может быть пустым
     * @return строка ведомости с исправленной оценкой
     * @throws domain.exception.PosException если нарушено правило модели:
     *         нет студента, критерия или прежней оценки, балл вне пределов
     */
    public GradeLine correct(int studentId, int criterionId, int points,
                             String comment) {
        Rubric rubric = rubrics.get();
        Student student = students.get(new StudentId(studentId));
        Evaluation corrected = evaluations.create(rubric,
                new CriterionId(criterionId), points, comment);
        student.replace(corrected);
        students.save(student);
        return line(rubric, corrected);
    }

    /**
     * Отменяет оценку.
     *
     * @param studentId   номер студента
     * @param criterionId номер критерия
     * @return строка ведомости с отменённой оценкой — для подтверждения
     * @throws domain.exception.PosException если нет студента, критерия
     *         или оценки
     */
    public GradeLine cancel(int studentId, int criterionId) {
        Rubric rubric = rubrics.get();
        Student student = students.get(new StudentId(studentId));
        Evaluation cancelled = student.cancel(new CriterionId(criterionId));
        students.save(student);
        return line(rubric, cancelled);
    }

    /**
     * Возвращает оценки студента по всем критериям.
     *
     * @param studentId номер студента
     * @return ведомость студента
     * @throws domain.exception.UnknownStudentException если студента нет
     */
    public StudentCard card(int studentId) {
        Student student = students.get(new StudentId(studentId));
        return new StudentCard(StudentView.of(student),
                calculator.lines(rubrics.get(), student));
    }

    /**
     * Строит строку ведомости для подтверждения в выводе.
     *
     * @param rubric     рубрикатор
     * @param evaluation оценка
     * @return строка ведомости
     */
    private static GradeLine line(Rubric rubric, Evaluation evaluation) {
        return GradeLine.graded(rubric.criterion(evaluation.getCriterionId()),
                evaluation);
    }
}

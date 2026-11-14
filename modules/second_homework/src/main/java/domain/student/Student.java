package domain.student;

import domain.exception.DuplicateEvaluationException;
import domain.exception.MissingEvaluationException;
import domain.exception.RemovalBlockedException;
import domain.rubric.CriterionId;
import domain.shared.Texts;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Студент — тот, кого оценивают; корень агрегата со своими оценками.
 *
 * <p>Сущность: идентичность — номер, равенство — по номеру. Два студента
 * с одинаковым ФИО — разные люди.
 *
 * <p>Оценки входят в агрегат, потому что их главное правило касается
 * одного студента: по каждому критерию не больше одной оценки. Выставить,
 * исправить и отменить оценку можно только через корень, поэтому правило
 * нельзя обойти. С рубрикатором оценка связана по номеру критерия, а не
 * ссылкой: агрегаты ссылаются друг на друга только по идентификаторам.
 */
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Student {

    /** Предельная длина ФИО. */
    public static final int MAX_NAME_LENGTH = 100;

    /** Номер на курсе. */
    @Getter
    @EqualsAndHashCode.Include
    private final StudentId id;

    /** ФИО. */
    @Getter
    private final String fullName;

    /** Оценки по номеру критерия в порядке выставления. */
    private final Map<CriterionId, Evaluation> evaluations =
            new LinkedHashMap<>();

    /**
     * Создаёт студента без оценок.
     *
     * @param id       номер на курсе
     * @param fullName непустое ФИО
     * @throws domain.exception.InvalidTextException если ФИО некорректно
     */
    public Student(@NonNull StudentId id, String fullName) {
        this.id = id;
        this.fullName = Texts.requireText(fullName, "ФИО студента",
                MAX_NAME_LENGTH);
    }

    /**
     * Выставляет новую оценку.
     *
     * @param evaluation оценка, созданная {@link EvaluationFactory}
     * @throws DuplicateEvaluationException если по критерию оценка уже есть
     */
    public void record(@NonNull Evaluation evaluation) {
        CriterionId criterionId = evaluation.getCriterionId();
        if (evaluations.putIfAbsent(criterionId, evaluation) != null) {
            throw new DuplicateEvaluationException(criterionId.value());
        }
    }

    /**
     * Исправляет оценку: заменяет прежнюю по тому же критерию.
     *
     * @param corrected новая оценка, созданная {@link EvaluationFactory}
     * @return прежняя оценка
     * @throws MissingEvaluationException если исправлять нечего
     */
    public Evaluation replace(@NonNull Evaluation corrected) {
        CriterionId criterionId = corrected.getCriterionId();
        Evaluation previous = require(criterionId);
        evaluations.put(criterionId, corrected);
        return previous;
    }

    /**
     * Отменяет оценку по критерию.
     *
     * @param criterionId номер критерия
     * @return отменённая оценка
     * @throws MissingEvaluationException если отменять нечего
     */
    public Evaluation cancel(@NonNull CriterionId criterionId) {
        Evaluation cancelled = require(criterionId);
        evaluations.remove(criterionId);
        return cancelled;
    }

    /**
     * Находит оценку по критерию.
     *
     * @param criterionId номер критерия
     * @return оценка или пустой {@code Optional}
     */
    public Optional<Evaluation> evaluation(@NonNull CriterionId criterionId) {
        return Optional.ofNullable(evaluations.get(criterionId));
    }

    /**
     * Возвращает все оценки.
     *
     * @return неизменяемый список в порядке выставления
     */
    public List<Evaluation> evaluations() {
        return List.copyOf(evaluations.values());
    }

    /**
     * Сообщает, есть ли оценка по критерию.
     *
     * @param criterionId номер критерия
     * @return {@code true}, если оценка выставлена
     */
    public boolean hasEvaluationFor(@NonNull CriterionId criterionId) {
        return evaluations.containsKey(criterionId);
    }

    /**
     * Проверяет, что студента можно удалить с курса.
     *
     * @throws RemovalBlockedException если у студента есть оценки
     */
    public void ensureRemovable() {
        if (!evaluations.isEmpty()) {
            throw new RemovalBlockedException(
                    "студента «" + fullName + "»");
        }
    }

    /**
     * Находит оценку, которая обязана существовать.
     *
     * @param criterionId номер критерия
     * @return оценка
     * @throws MissingEvaluationException если оценки нет
     */
    private Evaluation require(CriterionId criterionId) {
        Evaluation evaluation = evaluations.get(criterionId);
        if (evaluation == null) {
            throw new MissingEvaluationException(criterionId.value());
        }
        return evaluation;
    }
}

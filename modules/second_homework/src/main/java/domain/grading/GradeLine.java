package domain.grading;

import domain.rubric.Criterion;
import domain.student.Evaluation;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Строка ведомости: критерий рубрикатора и оценка по нему, если есть.
 *
 * <p>Соединяет два агрегата — рубрикатор и студента — для расчёта итога
 * и для вывода. Инвариант: оценка, если есть, относится именно к этому
 * критерию. Его гарантирует закрытый конструктор: создать строку можно
 * только через {@link #graded} и {@link #ungraded}.
 */
// Обычный класс, а не record: конструктор записи нельзя закрыть,
// а Optional остаётся только в возвращаемом значении, а не в поле.
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public final class GradeLine {

    /** Критерий рубрикатора. */
    @Getter
    private final Criterion criterion;

    /** Оценка по критерию; {@code null}, если не выставлена. */
    private final Evaluation evaluation;

    /**
     * Строка с выставленной оценкой.
     *
     * @param criterion  критерий
     * @param evaluation оценка по этому критерию
     * @return строка ведомости
     * @throws IllegalArgumentException если оценка относится к другому
     *                                  критерию — ошибка в коде
     */
    public static GradeLine graded(@NonNull Criterion criterion,
                                   @NonNull Evaluation evaluation) {
        if (!criterion.getId().equals(evaluation.getCriterionId())) {
            throw new IllegalArgumentException("Оценка по критерию №"
                    + evaluation.getCriterionId().value()
                    + " не относится к критерию №"
                    + criterion.getId().value());
        }
        return new GradeLine(criterion, evaluation);
    }

    /**
     * Строка без оценки.
     *
     * @param criterion критерий
     * @return строка ведомости
     */
    public static GradeLine ungraded(@NonNull Criterion criterion) {
        return new GradeLine(criterion, null);
    }

    /**
     * Возвращает оценку по критерию.
     *
     * @return оценка или пустой {@code Optional}
     */
    public Optional<Evaluation> getEvaluation() {
        return Optional.ofNullable(evaluation);
    }

    /**
     * Сообщает, выставлена ли оценка.
     *
     * @return {@code true}, если оценка есть
     */
    public boolean isGraded() {
        return evaluation != null;
    }
}

package domain.grading;

import java.math.BigDecimal;
import java.util.List;

/**
 * Текущая успеваемость: учитываются только выставленные оценки.
 *
 * <p>Нужна в середине курса: экзамен ещё не прошёл, и считать его нулём
 * значило бы показывать всем «неуд». Веса нормируются на сумму весов
 * оценённых критериев.
 */
public final class GradedOnlyPolicy implements GradingPolicy {

    /** Код правила. */
    public static final String CODE = "current";

    /**
     * {@inheritDoc}
     */
    @Override
    public String code() {
        return CODE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String description() {
        return "текущая успеваемость (только выставленные оценки)";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal grade(List<GradeLine> lines) {
        return WeightedMean.onTenPointScale(lines.stream()
                .filter(GradeLine::isGraded)
                .toList());
    }
}

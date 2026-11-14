package domain.grading;

import java.math.BigDecimal;
import java.util.List;

/**
 * Итог по всему рубрикатору: невыставленная оценка считается нулём.
 *
 * <p>Официальное правило в конце курса: студент, не сдавший работу,
 * не должен получать итог только по сданным.
 */
public final class FullRubricPolicy implements GradingPolicy {

    /** Код правила. */
    public static final String CODE = "full";

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
        return "итог по всему рубрикатору (нет оценки — 0)";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal grade(List<GradeLine> lines) {
        return WeightedMean.onTenPointScale(lines);
    }
}

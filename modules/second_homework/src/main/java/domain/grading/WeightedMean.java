package domain.grading;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

/**
 * Взвешенное среднее долей баллов по десятибалльной шкале.
 *
 * <p>Общая арифметика правил итога: правила отличаются тем, какие строки
 * ведомости учитывать, а формула у них одна:
 * {@code 10 · Σ(вес · балл / максимум) / Σ вес}. Строка без оценки
 * считается нулём.
 */
final class WeightedMean {

    /** Точность промежуточных вычислений. */
    private static final MathContext PRECISION = MathContext.DECIMAL64;

    /**
     * Закрытый конструктор: класс содержит только статические методы.
     */
    private WeightedMean() {
    }

    /**
     * Считает взвешенное среднее по десятибалльной шкале.
     *
     * @param lines учитываемые строки ведомости
     * @return итог от 0 до 10; ноль для пустого списка
     */
    static BigDecimal onTenPointScale(List<GradeLine> lines) {
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal weighted = BigDecimal.ZERO;
        for (GradeLine line : lines) {
            BigDecimal weight = line.getCriterion().getWeight().value();
            totalWeight = totalWeight.add(weight);
            BigDecimal ratio = line.getEvaluation()
                    .map(evaluation -> evaluation.getScore().ratio())
                    .orElse(BigDecimal.ZERO);
            weighted = weighted.add(weight.multiply(ratio, PRECISION));
        }
        if (totalWeight.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return weighted.divide(totalWeight, PRECISION)
                .multiply(FinalGrade.MAX, PRECISION);
    }
}

package domain.grading;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.NonNull;
import lombok.ToString;

/**
 * Округление поверх любого правила итога (структурный паттерн
 * «Декоратор»).
 *
 * <p>Округление не зависит от того, как итог получен, поэтому оно
 * не дублируется в каждой стратегии и не размазывается по консоли
 * и экспорту: правило оборачивается один раз при сборке приложения.
 * Точность у правил разная — официальный итог округляется до целого
 * по десятибалльной шкале, текущая успеваемость до сотых, — а класс один.
 *
 * <p>Округление арифметическое: 7,5 → 8.
 */
@ToString
public final class RoundedPolicy implements GradingPolicy {

    /** Наибольшее число знаков после запятой. */
    public static final int MAX_SCALE = 4;

    /** Режим округления: арифметический. */
    private static final RoundingMode MODE = RoundingMode.HALF_UP;

    /** Оборачиваемое правило. */
    private final GradingPolicy inner;

    /** Число знаков после запятой. */
    private final int scale;

    /**
     * Оборачивает правило.
     *
     * @param inner правило, итог которого округляется
     * @param scale знаков после запятой, от 0 до {@value #MAX_SCALE}
     * @throws IllegalArgumentException если точность вне диапазона —
     *                                  ошибка конфигурации
     */
    public RoundedPolicy(@NonNull GradingPolicy inner, int scale) {
        if (scale < 0 || scale > MAX_SCALE) {
            throw new IllegalArgumentException(
                    "Точность округления должна быть от 0 до " + MAX_SCALE
                            + ": " + scale);
        }
        this.inner = inner;
        this.scale = scale;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Код не меняется: округление — свойство подачи, а не другое
     * правило.
     */
    @Override
    public String code() {
        return inner.code();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String description() {
        String precision = scale == 0
                ? "до целого"
                : "до " + scale + " зн. после запятой";
        return inner.description() + ", округление " + precision;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal grade(List<GradeLine> lines) {
        return inner.grade(lines).setScale(scale, MODE);
    }
}

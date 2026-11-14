package domain.rubric;

import domain.exception.InvalidWeightException;
import java.math.BigDecimal;
import lombok.NonNull;

/**
 * Вес критерия — его вклад в итог.
 *
 * <p>Веса нормируются на их сумму при расчёте итога, поэтому их можно
 * задавать и долями (0.3), и процентами (30): важно только соотношение.
 * Сумма весов не обязана равняться единице — иначе рубрикатор нельзя
 * было бы собирать по одному критерию.
 *
 * <p>Объект-значение. Конечные нули отбрасываются, поэтому
 * {@code 0.30} и {@code 0.3} — один и тот же вес.
 *
 * @param value положительный вес, не больше {@link #MAX}
 */
public record Weight(@NonNull BigDecimal value) {

    /** Наибольший допустимый вес. */
    public static final BigDecimal MAX = BigDecimal.valueOf(100);

    /**
     * Проверяет и нормализует вес.
     *
     * @throws InvalidWeightException если вес не положителен
     *                                или больше {@link #MAX}
     */
    public Weight {
        if (value.signum() <= 0 || value.compareTo(MAX) > 0) {
            throw new InvalidWeightException(value, MAX);
        }
        value = value.stripTrailingZeros();
    }

    /**
     * Создаёт вес из строки, например {@code "0.3"}.
     *
     * @param value десятичная запись веса с точкой
     * @return вес
     * @throws NumberFormatException  если строка не число
     * @throws InvalidWeightException если вес вне диапазона
     */
    public static Weight of(@NonNull String value) {
        return new Weight(new BigDecimal(value));
    }
}

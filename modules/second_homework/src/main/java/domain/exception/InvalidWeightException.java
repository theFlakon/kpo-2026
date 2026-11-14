package domain.exception;

import java.io.Serial;
import java.math.BigDecimal;
import lombok.Getter;

/**
 * Вес критерия не положителен или слишком велик.
 */
@Getter
public final class InvalidWeightException extends InvalidValueException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /** Отклонённое значение. */
    private final BigDecimal value;

    /**
     * Создаёт исключение.
     *
     * @param value отклонённое значение
     * @param max   верхняя граница, включительно
     */
    public InvalidWeightException(BigDecimal value, BigDecimal max) {
        super("Вес критерия должен быть больше 0 и не больше "
                + max.toPlainString() + ": " + value.toPlainString());
        this.value = value;
    }
}

package domain.exception;

import java.io.Serial;
import lombok.Getter;

/**
 * Номер студента или критерия не положителен.
 */
@Getter
public final class InvalidIdentifierException extends InvalidValueException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /** Отклонённое значение. */
    private final int value;

    /**
     * Создаёт исключение.
     *
     * @param owner чей номер, в родительном падеже: «студента», «критерия»
     * @param value отклонённое значение
     */
    public InvalidIdentifierException(String owner, int value) {
        super("Номер " + owner + " должен быть положительным: " + value);
        this.value = value;
    }
}

package domain.exception;

import java.io.Serial;
import lombok.Getter;

/**
 * Инвентарный номер не положителен.
 */
@Getter
public final class InvalidInventoryNumberException
        extends InvalidAssetDataException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /** Отклонённое значение номера. */
    private final int value;

    /**
     * Создаёт исключение для некорректного номера.
     *
     * @param value отклонённое значение
     */
    public InvalidInventoryNumberException(int value) {
        super("Инвентарный номер должен быть положительным: " + value);
        this.value = value;
    }
}
package domain.exception;

import java.io.Serial;
import lombok.Getter;

/**
 * Название объекта отсутствует или состоит только из пробелов.
 */
@Getter
public final class InvalidNameException extends InvalidAssetDataException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /** Отклонённое значение; может быть {@code null}. */
    private final String value;

    /**
     * Создаёт исключение для некорректного названия.
     *
     * @param value отклонённое значение, допускается {@code null}
     */
    public InvalidNameException(String value) {
        super("Название не может быть пустым: '" + value + "'");
        this.value = value;
    }
}
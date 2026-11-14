package domain.exception;

import java.io.Serial;
import lombok.Getter;

/**
 * Текстовое поле пустое или слишком длинное.
 *
 * <p>Одно исключение на все текстовые поля модели (ФИО, название
 * критерия, комментарий): правило у них общее, отличается только поле.
 */
@Getter
public final class InvalidTextException extends InvalidValueException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /** Поле с ошибкой, например «Название критерия». */
    private final String field;

    /**
     * Создаёт исключение с готовым сообщением.
     *
     * @param field   поле с ошибкой
     * @param message сообщение для пользователя
     */
    private InvalidTextException(String field, String message) {
        super(message);
        this.field = field;
    }

    /**
     * Исключение для пустого значения.
     *
     * @param field поле с ошибкой
     * @return исключение
     */
    public static InvalidTextException blank(String field) {
        return new InvalidTextException(field,
                field + " не может быть пустым");
    }

    /**
     * Исключение для слишком длинного значения.
     *
     * @param field     поле с ошибкой
     * @param maxLength допустимая длина
     * @return исключение
     */
    public static InvalidTextException tooLong(String field, int maxLength) {
        return new InvalidTextException(field,
                field + ": не длиннее " + maxLength + " символов");
    }
}

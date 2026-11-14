package domain.exception;

import java.io.Serial;
import lombok.Getter;

/**
 * Максимальный балл критерия вне допустимого диапазона.
 *
 * <p>Границы передаются в конструктор, чтобы пакет исключений
 * не зависел от модели и не возникал цикл зависимостей.
 */
@Getter
public final class InvalidMaxScoreException extends InvalidValueException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /** Отклонённое значение. */
    private final int value;

    /**
     * Создаёт исключение.
     *
     * @param value отклонённое значение
     * @param min   нижняя граница, включительно
     * @param max   верхняя граница, включительно
     */
    public InvalidMaxScoreException(int value, int min, int max) {
        super("Максимальный балл критерия должен быть от " + min + " до "
                + max + ": " + value);
        this.value = value;
    }
}

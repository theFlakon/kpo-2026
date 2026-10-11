package domain.exception;

import java.io.Serial;
import lombok.Getter;

/**
 * Простота для новичка вне допустимого диапазона.
 *
 * <p>Границы передаются в конструктор, а не берутся из
 * {@code Vehicle}: иначе пакет исключений зависел бы от пакета
 * транспорта, а тот — от исключений, и получился бы цикл.
 */
@Getter
public final class InvalidSimplicityException
        extends InvalidAssetDataException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /** Отклонённое значение простоты. */
    private final int value;

    /** Нижняя допустимая граница, включительно. */
    private final int min;

    /** Верхняя допустимая граница, включительно. */
    private final int max;

    /**
     * Создаёт исключение для простоты вне диапазона.
     *
     * @param value отклонённое значение
     * @param min   нижняя граница, включительно
     * @param max   верхняя граница, включительно
     */
    public InvalidSimplicityException(int value, int min, int max) {
        super("Простота должна быть от " + min + " до " + max
                + ": " + value);
        this.value = value;
        this.min = min;
        this.max = max;
    }
}
package domain.exception;

import java.io.Serial;
import lombok.Getter;

/**
 * Оценка технического состояния в заявке вне допустимого диапазона.
 *
 * <p>Наследует {@link VyshKatException} напрямую, а не
 * {@link InvalidAssetDataException}: состояние описывает результат осмотра,
 * а не объект на балансе, и в сам объект не попадает.
 *
 * <p>Границы передаются в конструктор, чтобы пакет исключений
 * не зависел от {@code IntakeRequest}.
 */
@Getter
public final class InvalidTechnicalConditionException
        extends VyshKatException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /** Отклонённое значение состояния, %. */
    private final int value;

    /** Нижняя допустимая граница, включительно. */
    private final int min;

    /** Верхняя допустимая граница, включительно. */
    private final int max;

    /**
     * Создаёт исключение для состояния вне диапазона.
     *
     * @param value отклонённое значение, %
     * @param min   нижняя граница, включительно
     * @param max   верхняя граница, включительно
     */
    public InvalidTechnicalConditionException(int value, int min, int max) {
        super("Техническое состояние должно быть от " + min + " до " + max
                + "%: " + value);
        this.value = value;
        this.min = min;
        this.max = max;
    }
}
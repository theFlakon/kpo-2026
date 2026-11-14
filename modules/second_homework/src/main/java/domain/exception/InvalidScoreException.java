package domain.exception;

import java.io.Serial;
import lombok.Getter;

/**
 * Балл выходит за пределы критерия, например «12 из 10».
 */
@Getter
public final class InvalidScoreException extends InvalidValueException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /** Отклонённый балл. */
    private final int points;

    /** Максимум критерия. */
    private final int maxPoints;

    /**
     * Создаёт исключение.
     *
     * @param points    отклонённый балл
     * @param maxPoints максимум критерия
     */
    public InvalidScoreException(int points, int maxPoints) {
        super("Балл должен быть от 0 до " + maxPoints + ": " + points);
        this.points = points;
        this.maxPoints = maxPoints;
    }
}

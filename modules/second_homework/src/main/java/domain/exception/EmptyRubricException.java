package domain.exception;

import java.io.Serial;

/**
 * Итог нельзя посчитать: в рубрикаторе нет ни одного критерия.
 */
public final class EmptyRubricException extends PosException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Создаёт исключение с фиксированным сообщением.
     */
    public EmptyRubricException() {
        super("Рубрикатор пуст: итог не из чего считать");
    }
}

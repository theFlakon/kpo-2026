package domain.exception;

import java.io.Serial;
import lombok.AccessLevel;
import lombok.experimental.StandardException;

/**
 * Значение нарушает инвариант объекта-значения или сущности.
 *
 * <p>Бросается из конструкторов модели: некорректный объект
 * не может существовать.
 */
@StandardException(access = AccessLevel.PROTECTED)
public abstract class InvalidValueException extends PosException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;
}

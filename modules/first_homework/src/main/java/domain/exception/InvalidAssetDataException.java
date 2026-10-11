package domain.exception;

import java.io.Serial;
import lombok.AccessLevel;
import lombok.experimental.StandardException;

/**
 * Данные объекта на балансе нарушают его контракт.
 *
 * <p>Бросается из конструкторов транспорта и вещей. Общий предок нужен,
 * чтобы вызывающий код мог обработать «некорректные данные» целиком,
 * а тесты — проверить, какой именно инвариант нарушен.
 */
@StandardException(access = AccessLevel.PROTECTED)
public abstract class InvalidAssetDataException extends VyshKatException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;
}
package domain.exception;

import java.io.Serial;
import lombok.AccessLevel;
import lombok.experimental.StandardException;

/**
 * Корень иерархии исключений модуля «ПОС».
 *
 * <p>Исключение непроверяемое: нарушенный инвариант вызывающий код
 * на месте не исправит, а проверяемые исключения засорили бы сигнатуры
 * всех слоёв, ничего не дав взамен.
 *
 * <p>Класс абстрактный: бросать нужно конкретного наследника, чтобы
 * по типу было видно, какое правило нарушено.
 */
@StandardException(access = AccessLevel.PROTECTED)
public abstract class PosException extends RuntimeException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;
}

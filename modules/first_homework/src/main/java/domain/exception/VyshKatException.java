package domain.exception;

import java.io.Serial;
import lombok.AccessLevel;
import lombok.experimental.StandardException;

/**
 * Корень иерархии исключений проекта.
 *
 * <p>Исключение непроверяемое: нарушение инвариантов — это ошибка
 * входных данных, которую вызывающий код всё равно не исправит на месте.
 * Проверяемые исключения не сочетаются с функциональными интерфейсами
 * (например, со ссылками на конструкторы в {@code VehicleFactory})
 * и засорили бы сигнатуры, ничего не дав взамен.
 *
 * <p>Класс абстрактный: бросать нужно конкретного наследника, чтобы
 * по типу было видно, что именно пошло не так.
 */
@StandardException(access = AccessLevel.PROTECTED)
public abstract class VyshKatException extends RuntimeException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;
}
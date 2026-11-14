package adapter.in.console;

import java.io.Serial;

/**
 * Выбирать не из чего: например, на курсе ещё нет студентов.
 *
 * <p>Не ошибка данных, а подсказка пользователю, что сделать сначала.
 * Пункт меню прерывается, и консоль показывает подсказку.
 */
final class NothingToChooseException extends RuntimeException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Создаёт исключение.
     *
     * @param hint подсказка, например «Сначала добавьте студента»
     */
    NothingToChooseException(String hint) {
        super(hint);
    }
}

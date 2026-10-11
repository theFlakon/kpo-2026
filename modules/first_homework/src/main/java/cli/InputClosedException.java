package cli;

import java.io.Serial;

/**
 * Ввод закончился: пользователь нажал Ctrl+D или файл с вводом
 * прочитан до конца.
 *
 * <p>Не наследует {@code VyshKatException}: это не ошибка данных,
 * а сигнал консоли завершить работу. Иначе общий обработчик ошибок
 * в меню перехватил бы его и зациклился на пустом вводе.
 */
final class InputClosedException extends RuntimeException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Создаёт исключение с фиксированным сообщением.
     */
    InputClosedException() {
        super("Ввод закончился");
    }
}

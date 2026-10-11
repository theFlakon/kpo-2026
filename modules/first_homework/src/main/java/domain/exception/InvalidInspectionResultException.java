package domain.exception;

import java.io.Serial;

/**
 * Результат техосмотра создан без причины решения.
 *
 * <p>В отличие от остальных исключений пакета означает не ошибку
 * во входных данных, а нарушение контракта реализацией
 * {@code ServiceCenter}: решение всегда должно быть объяснимым.
 */
public final class InvalidInspectionResultException
        extends VyshKatException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Создаёт исключение с фиксированным сообщением.
     */
    public InvalidInspectionResultException() {
        super("У решения техосмотра должна быть непустая причина");
    }
}
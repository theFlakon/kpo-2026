package application;

import domain.exception.PosException;
import java.io.Serial;

/**
 * Выгрузить ведомость не удалось: например, нет прав на запись.
 *
 * <p>Объявлено в слое приложения рядом с портом
 * {@link GradeSheetExporter}: адаптер переводит свою техническую ошибку
 * в ошибку сценария, и консоль показывает её как любую другую.
 */
public final class ExportFailedException extends PosException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Создаёт исключение.
     *
     * @param destination куда пытались выгрузить
     * @param cause       исходная техническая ошибка
     */
    public ExportFailedException(String destination, Throwable cause) {
        super("Не удалось выгрузить итоги в " + destination + ": "
                + cause.getMessage(), cause);
    }
}

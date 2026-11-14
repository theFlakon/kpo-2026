package domain.exception;

import java.io.Serial;

/**
 * Студента или критерий нельзя удалить: по ним выставлены оценки.
 *
 * <p>Удаление вместе с оценками молча изменило бы итоги, поэтому
 * сначала оценки нужно явно отменить.
 */
public final class RemovalBlockedException extends PosException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Создаёт исключение.
     *
     * @param subject что удаляют, в винительном падеже:
     *                «студента «Анна Смирнова»»
     */
    public RemovalBlockedException(String subject) {
        super("Нельзя удалить " + subject + ": есть выставленные оценки");
    }
}

package domain.exception;

import java.io.Serial;
import lombok.Getter;

/**
 * Критерий с таким названием уже есть в рубрикаторе.
 *
 * <p>Два критерия «Экзамен» сделали бы ведомость неоднозначной.
 */
@Getter
public final class DuplicateCriterionException extends PosException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /** Повторяющееся название. */
    private final String name;

    /**
     * Создаёт исключение.
     *
     * @param name повторяющееся название
     */
    public DuplicateCriterionException(String name) {
        super("Критерий «" + name + "» уже есть в рубрикаторе");
        this.name = name;
    }
}

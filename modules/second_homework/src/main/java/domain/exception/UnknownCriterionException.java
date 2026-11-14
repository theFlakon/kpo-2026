package domain.exception;

import java.io.Serial;
import lombok.Getter;

/**
 * Критерия с таким номером нет в рубрикаторе.
 *
 * <p>Охраняет правило «оценку можно выставить только по критерию
 * из рубрикатора».
 */
@Getter
public final class UnknownCriterionException extends PosException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /** Запрошенный номер критерия. */
    private final int criterionId;

    /**
     * Создаёт исключение.
     *
     * @param criterionId запрошенный номер критерия
     */
    public UnknownCriterionException(int criterionId) {
        super("В рубрикаторе нет критерия №" + criterionId);
        this.criterionId = criterionId;
    }
}

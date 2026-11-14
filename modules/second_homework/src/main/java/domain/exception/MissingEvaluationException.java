package domain.exception;

import java.io.Serial;
import lombok.Getter;

/**
 * У студента нет оценки по критерию, которую просят исправить
 * или отменить.
 */
@Getter
public final class MissingEvaluationException extends PosException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /** Номер критерия. */
    private final int criterionId;

    /**
     * Создаёт исключение.
     *
     * @param criterionId номер критерия
     */
    public MissingEvaluationException(int criterionId) {
        super("Оценки по критерию №" + criterionId + " нет");
        this.criterionId = criterionId;
    }
}

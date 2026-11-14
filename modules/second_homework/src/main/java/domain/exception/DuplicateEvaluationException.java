package domain.exception;

import java.io.Serial;
import lombok.Getter;

/**
 * У студента уже есть оценка по этому критерию.
 *
 * <p>Повторное выставление молча заменило бы прежний балл; для этого
 * есть явные сценарии «исправить» и «отменить».
 */
@Getter
public final class DuplicateEvaluationException extends PosException {

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
    public DuplicateEvaluationException(int criterionId) {
        super("Оценка по критерию №" + criterionId
                + " уже выставлена: её можно исправить или отменить");
        this.criterionId = criterionId;
    }
}

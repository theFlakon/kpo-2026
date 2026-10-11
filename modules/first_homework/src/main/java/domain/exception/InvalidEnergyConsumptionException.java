package domain.exception;

import java.io.Serial;
import lombok.Getter;

/**
 * Суточный расход энергии отрицателен, бесконечен или равен {@code NaN}.
 */
@Getter
public final class InvalidEnergyConsumptionException
        extends InvalidAssetDataException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /** Отклонённое значение расхода, кВт·ч. */
    private final double value;

    /**
     * Создаёт исключение для некорректного расхода.
     *
     * @param value отклонённое значение, кВт·ч
     */
    public InvalidEnergyConsumptionException(double value) {
        super("Суточный расход должен быть конечным неотрицательным "
                + "числом: " + value);
        this.value = value;
    }
}
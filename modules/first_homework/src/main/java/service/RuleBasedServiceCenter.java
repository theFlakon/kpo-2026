package service;

import domain.inspection.InspectionResult;
import domain.inspection.IntakeRequest;
import domain.inspection.ServiceCenter;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Техосмотр по фиксированным правилам.
 *
 * <p>Технику принимают, если исправны тормоза и техническое состояние
 * не ниже порога. При отказе в причине перечисляются все нарушения
 * сразу, а не только первое: механику нужен полный список проблем.
 */
@ToString
public final class RuleBasedServiceCenter implements ServiceCenter {

    /** Порог состояния по умолчанию, %. */
    public static final int DEFAULT_MIN_CONDITION = 70;

    /** Разделитель причин отказа. */
    private static final String REASON_SEPARATOR = "; ";

    /**
     * Минимальное техническое состояние для приёмки, %.
     */
    @Getter
    private final int minCondition;

    /**
     * Создаёт техосмотр с заданным порогом состояния.
     *
     * @param minCondition минимальное состояние для приёмки, от 0 до 100 %
     * @throws domain.exception.InvalidTechnicalConditionException
     *         если порог вне диапазона
     */
    public RuleBasedServiceCenter(int minCondition) {
        this.minCondition = IntakeRequest.requireValidCondition(minCondition);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InspectionResult inspect(@NonNull IntakeRequest request) {
        List<String> problems = new ArrayList<>();
        if (!request.brakesOk()) {
            problems.add("тормоза неисправны");
        }
        if (request.technicalCondition() < minCondition) {
            problems.add("техническое состояние "
                    + request.technicalCondition()
                    + "% ниже порога " + minCondition + "%");
        }
        // Пустой список означает, что все правила пройдены.
        return problems.isEmpty()
                ? InspectionResult.pass()
                : InspectionResult.fail(
                String.join(REASON_SEPARATOR, problems));
    }
}

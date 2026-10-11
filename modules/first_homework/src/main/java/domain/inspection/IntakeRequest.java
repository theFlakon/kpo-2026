package domain.inspection;

import domain.capability.EnergyConsumer;
import domain.capability.Inventoriable;
import domain.exception.InvalidTechnicalConditionException;
import domain.vehicle.Vehicle;
import lombok.Builder;

/**
 * Заявка на приёмку техники в парк.
 *
 * <p>Заполняется при приёмке, проверяется {@link ServiceCenter},
 * после одобрения по ней создаётся объект транспорта.
 *
 * <p>Некорректная заявка не может существовать: все поля проверяются
 * в конструкторе теми же правилами, что и в доменных классах. Поэтому
 * до осмотра и выдачи инвентарного номера доходят только валидные
 * данные, и номер не «сгорает» на исключении в конструкторе транспорта.
 *
 * <p>Код вида транспорта здесь не проверяется: заявка не знает,
 * какие виды зарегистрированы, это задача {@code VehicleFactory}.
 *
 * @param vehicleType        код вида транспорта, например {@code "scooter"}
 * @param name               непустое название модели
 * @param simplicity         простота для новичка, от 1 до 10
 * @param dailyEnergyKwh     суточный расход, кВт·ч; для неэлектрических
 *                           видов игнорируется и по умолчанию равен 0
 * @param technicalCondition оценка состояния механиком, от
 *                           {@value #MIN_CONDITION} до
 *                           {@value #MAX_CONDITION} %
 * @param brakesOk           исправны ли тормоза
 */
// Builder читается лучше шести позиционных аргументов и вызывает
// канонический конструктор, поэтому проверки ниже не обходятся.
@Builder
public record IntakeRequest(
        String vehicleType,
        String name,
        int simplicity,
        double dailyEnergyKwh,
        int technicalCondition,
        boolean brakesOk) {

    /** Минимальная оценка технического состояния, %. */
    public static final int MIN_CONDITION = 0;

    /** Максимальная оценка технического состояния, %. */
    public static final int MAX_CONDITION = 100;

    /**
     * Проверяет поля заявки.
     *
     * @throws domain.exception.InvalidAssetDataException
     *         если название, простота или расход нарушают контракт
     *         доменных классов
     * @throws InvalidTechnicalConditionException
     *         если оценка состояния вне диапазона
     */
    public IntakeRequest {
        // Те же проверки, что в конструкторах транспорта: правило
        // записано в одном месте, вызывается в двух.
        Inventoriable.requireValidName(name);
        Vehicle.requireValidSimplicity(simplicity);
        EnergyConsumer.requireValidEnergy(dailyEnergyKwh);
        requireValidCondition(technicalCondition);
    }

    /**
     * Проверяет оценку технического состояния.
     *
     * <p>Публичный, потому что тот же диапазон проверяет порог
     * в {@code RuleBasedServiceCenter}.
     *
     * @param technicalCondition проверяемое значение, %
     * @return то же значение, если оно корректно
     * @throws InvalidTechnicalConditionException если значение вне диапазона
     */
    public static int requireValidCondition(int technicalCondition) {
        if (technicalCondition < MIN_CONDITION
                || technicalCondition > MAX_CONDITION) {
            throw new InvalidTechnicalConditionException(
                    technicalCondition, MIN_CONDITION, MAX_CONDITION);
        }
        return technicalCondition;
    }
}
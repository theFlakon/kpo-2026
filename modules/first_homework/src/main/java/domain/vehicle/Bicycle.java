package domain.vehicle;

import lombok.ToString;

/**
 * Обычный велосипед без электропривода.
 *
 * <p>Не реализует {@link domain.capability.EnergyConsumer}, поэтому
 * не учитывается в отчёте по энергопотреблению парка.
 */
@ToString(callSuper = true)
public final class Bicycle extends Vehicle {

    /**
     * Создаёт велосипед.
     *
     * @param inventoryNumber положительный инвентарный номер
     * @param name            непустое название модели
     * @param simplicity      простота для новичка, от 1 до 10
     * @throws domain.exception.InvalidAssetDataException
     *         если любое из значений нарушает контракт
     */
    public Bicycle(int inventoryNumber, String name, int simplicity) {
        super(inventoryNumber, name, simplicity);
    }
}
package domain.item;

import lombok.ToString;

/**
 * Защитный шлем, выдаваемый вместе с транспортом.
 *
 * <p>Энергию не потребляет, поэтому не реализует
 * {@link domain.capability.EnergyConsumer}.
 */
@ToString(callSuper = true)
public final class Helmet extends Item {

    /**
     * Создаёт шлем.
     *
     * @param inventoryNumber положительный инвентарный номер
     * @param name            непустое название
     * @throws domain.exception.InvalidAssetDataException
     *         если номер или название нарушают контракт
     */
    public Helmet(int inventoryNumber, String name) {
        super(inventoryNumber, name);
    }
}
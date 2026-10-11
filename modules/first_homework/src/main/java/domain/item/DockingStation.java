package domain.item;

import domain.capability.EnergyConsumer;
import lombok.Getter;
import lombok.ToString;

/**
 * Док-станция: парковка и подзарядка транспорта на улице.
 *
 * <p>Вещь, а не транспорт, но потребляет энергию: совмещает роли
 * {@link domain.capability.Inventoriable} (через {@link Item})
 * и {@link EnergyConsumer} и поэтому учитывается в отчёте по энергии
 * наравне с электротранспортом.
 */
@Getter
@ToString(callSuper = true)
public final class DockingStation extends Item implements EnergyConsumer {

    /** Суточный расход электроэнергии, кВт·ч. */
    private final double dailyEnergyKwh;

    /**
     * Создаёт док-станцию с проверкой всех инвариантов.
     *
     * @param inventoryNumber положительный инвентарный номер
     * @param name            непустое название
     * @param dailyEnergyKwh  неотрицательный суточный расход, кВт·ч
     * @throws domain.exception.InvalidAssetDataException
     *         если любое из значений нарушает контракт
     */
    public DockingStation(int inventoryNumber, String name,
                          double dailyEnergyKwh) {
        super(inventoryNumber, name);
        this.dailyEnergyKwh =
                EnergyConsumer.requireValidEnergy(dailyEnergyKwh);
    }
}
package domain.item;

import domain.capability.EnergyConsumer;
import lombok.Getter;
import lombok.ToString;

/**
 * Зарядный шкаф для аккумуляторов на складе.
 *
 * <p>Вещь, а не транспорт, но потребляет энергию: совмещает роли
 * {@link domain.capability.Inventoriable} (через {@link Item})
 * и {@link EnergyConsumer} и поэтому учитывается в отчёте по энергии
 * наравне с электротранспортом.
 */
@Getter
@ToString(callSuper = true)
public final class ChargingCabinet extends Item implements EnergyConsumer {

    /** Суточный расход электроэнергии, кВт·ч. */
    private final double dailyEnergyKwh;

    /**
     * Создаёт зарядный шкаф с проверкой всех инвариантов.
     *
     * @param inventoryNumber положительный инвентарный номер
     * @param name            непустое название
     * @param dailyEnergyKwh  неотрицательный суточный расход, кВт·ч
     * @throws domain.exception.InvalidAssetDataException
     *         если любое из значений нарушает контракт
     */
    public ChargingCabinet(int inventoryNumber, String name,
                           double dailyEnergyKwh) {
        super(inventoryNumber, name);
        this.dailyEnergyKwh =
                EnergyConsumer.requireValidEnergy(dailyEnergyKwh);
    }
}
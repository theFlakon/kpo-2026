package domain.vehicle;

import domain.capability.EnergyConsumer;
import lombok.Getter;
import lombok.ToString;

/**
 * Электровелосипед с вспомогательным электроприводом.
 *
 * <p>Совмещает две роли: {@link domain.capability.Inventoriable}
 * (через {@link Vehicle}) и {@link EnergyConsumer}.
 */
@Getter
@ToString(callSuper = true)
public final class ElectricBike extends Vehicle implements EnergyConsumer {

    /** Суточный расход электроэнергии, кВт·ч. */
    private final double dailyEnergyKwh;

    /**
     * Создаёт электровелосипед с проверкой всех инвариантов.
     *
     * @param inventoryNumber положительный инвентарный номер
     * @param name            непустое название модели
     * @param simplicity      простота для новичка, от 1 до 10
     * @param dailyEnergyKwh  неотрицательный суточный расход, кВт·ч
     * @throws domain.exception.InvalidAssetDataException
     *         если любое из значений нарушает контракт
     */
    public ElectricBike(int inventoryNumber, String name, int simplicity,
                        double dailyEnergyKwh) {
        super(inventoryNumber, name, simplicity);
        this.dailyEnergyKwh =
                EnergyConsumer.requireValidEnergy(dailyEnergyKwh);
    }
}
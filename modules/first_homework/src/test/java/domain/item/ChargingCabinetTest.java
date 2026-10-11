package domain.item;

import domain.capability.EnergyConsumer;
import domain.capability.Inventoriable;
import org.junit.jupiter.api.DisplayName;
import support.contract.EnergyConsumerContract;
import support.contract.InventoriableContract;

/**
 * ChargingCabinet: вещь, которая потребляет энергию, — проходит оба контракта.
 */
@DisplayName("ChargingCabinet")
class ChargingCabinetTest implements InventoriableContract, EnergyConsumerContract {

    @Override
    public Inventoriable create(int inventoryNumber, String name) {
        return new ChargingCabinet(inventoryNumber, name, 1.5);
    }

    @Override
    public EnergyConsumer createWithEnergy(double dailyEnergyKwh) {
        return new ChargingCabinet(1, "Тестовый", dailyEnergyKwh);
    }
}

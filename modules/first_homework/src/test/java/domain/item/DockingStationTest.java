package domain.item;

import domain.capability.EnergyConsumer;
import domain.capability.Inventoriable;
import org.junit.jupiter.api.DisplayName;
import support.contract.EnergyConsumerContract;
import support.contract.InventoriableContract;

/**
 * DockingStation: вещь, которая потребляет энергию, — проходит оба контракта.
 */
@DisplayName("DockingStation")
class DockingStationTest implements InventoriableContract, EnergyConsumerContract {

    @Override
    public Inventoriable create(int inventoryNumber, String name) {
        return new DockingStation(inventoryNumber, name, 1.5);
    }

    @Override
    public EnergyConsumer createWithEnergy(double dailyEnergyKwh) {
        return new DockingStation(1, "Тестовый", dailyEnergyKwh);
    }
}

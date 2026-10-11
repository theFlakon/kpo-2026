package domain.vehicle;

import domain.capability.EnergyConsumer;
import domain.capability.Inventoriable;
import org.junit.jupiter.api.DisplayName;
import support.contract.EnergyConsumerContract;
import support.contract.InventoriableContract;

/**
 * ElectricBike: совмещает обе роли, поэтому проходит оба контракта.
 *
 * <p>Собственных тестов нет: всё поведение электровелосипеда описано ролями.
 * Специфичные проверки добавляются сюда обычными методами с {@code @Test}.
 */
@DisplayName("ElectricBike")
class ElectricBikeTest implements InventoriableContract, EnergyConsumerContract {

    @Override
    public Inventoriable create(int inventoryNumber, String name) {
        return new ElectricBike(inventoryNumber, name, 5, 0.4);
    }

    @Override
    public EnergyConsumer createWithEnergy(double dailyEnergyKwh) {
        return new ElectricBike(1, "Тестовый", 5, dailyEnergyKwh);
    }
}

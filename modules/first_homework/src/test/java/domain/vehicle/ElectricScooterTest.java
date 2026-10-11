package domain.vehicle;

import domain.capability.EnergyConsumer;
import domain.capability.Inventoriable;
import org.junit.jupiter.api.DisplayName;
import support.contract.EnergyConsumerContract;
import support.contract.InventoriableContract;

/**
 * ElectricScooter: совмещает обе роли, поэтому проходит оба контракта.
 *
 * <p>Собственных тестов нет: всё поведение электросамоката описано ролями.
 * Специфичные проверки добавляются сюда обычными методами с {@code @Test}.
 */
@DisplayName("ElectricScooter")
class ElectricScooterTest implements InventoriableContract, EnergyConsumerContract {

    @Override
    public Inventoriable create(int inventoryNumber, String name) {
        return new ElectricScooter(inventoryNumber, name, 5, 0.4);
    }

    @Override
    public EnergyConsumer createWithEnergy(double dailyEnergyKwh) {
        return new ElectricScooter(1, "Тестовый", 5, dailyEnergyKwh);
    }
}

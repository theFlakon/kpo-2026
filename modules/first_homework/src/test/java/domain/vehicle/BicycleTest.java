package domain.vehicle;

import static org.junit.jupiter.api.Assertions.assertFalse;

import domain.capability.EnergyConsumer;
import domain.capability.Inventoriable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.contract.InventoriableContract;

/**
 * Велосипед: стоит на балансе, но энергию не потребляет.
 */
@DisplayName("Bicycle")
class BicycleTest implements InventoriableContract {

    @Override
    public Inventoriable create(int inventoryNumber, String name) {
        return new Bicycle(inventoryNumber, name, 5);
    }

    @Test
    @DisplayName("не является потребителем энергии")
    void isNotEnergyConsumer() {
        // instanceof не скомпилировался бы: класс final и не реализует
        // интерфейс. Проверяем на уровне типов.
        assertFalse(EnergyConsumer.class.isAssignableFrom(Bicycle.class));
    }
}

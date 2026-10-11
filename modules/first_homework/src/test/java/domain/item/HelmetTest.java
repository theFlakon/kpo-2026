package domain.item;

import static org.junit.jupiter.api.Assertions.assertFalse;

import domain.capability.EnergyConsumer;
import domain.capability.Inventoriable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.contract.InventoriableContract;

/**
 * Шлем: вещь на балансе без энергопотребления.
 */
@DisplayName("Helmet")
class HelmetTest implements InventoriableContract {

    @Override
    public Inventoriable create(int inventoryNumber, String name) {
        return new Helmet(inventoryNumber, name);
    }

    @Test
    @DisplayName("не является потребителем энергии")
    void isNotEnergyConsumer() {
        assertFalse(EnergyConsumer.class.isAssignableFrom(Helmet.class));
    }
}

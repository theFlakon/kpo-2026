package service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Генератор выдаёт номера по порядку, начиная с единицы.
 */
@DisplayName("InventoryNumberGenerator")
class InventoryNumberGeneratorTest {

    @Test
    @DisplayName("выдаёт 1, 2, 3 …")
    void issuesSequentialNumbers() {
        InventoryNumberGenerator generator = new InventoryNumberGenerator();

        assertEquals(1, generator.next());
        assertEquals(2, generator.next());
        assertEquals(3, generator.next());
    }

    @Test
    @DisplayName("каждый генератор начинает с единицы — тесты не влияют друг на друга")
    void instancesAreIndependent() {
        InventoryNumberGenerator first = new InventoryNumberGenerator();
        first.next();
        first.next();

        assertEquals(1, new InventoryNumberGenerator().next());
    }
}

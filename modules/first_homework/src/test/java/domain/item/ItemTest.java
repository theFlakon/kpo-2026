package domain.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.TestData;

/**
 * Общее поведение вещей: равенство по номеру внутри ветки вещей.
 */
@DisplayName("Item")
class ItemTest {

    @Test
    @DisplayName("вещи с одним номером равны, даже если виды разные")
    void equalityByNumberWithinItems() {
        assertEquals(TestData.helmet(5), TestData.cabinet(5, 2.0));
    }

    // Фиксирует осознанное следствие варианта без общего предка: равенство
    // не пересекает ветки, поэтому уникальность номеров по всему парку
    // обеспечивает репозиторий (см. InMemoryInventoryRepositoryTest).
    @Test
    @DisplayName("вещь и транспорт с одним номером не равны")
    void itemNeverEqualsVehicle() {
        assertNotEquals(TestData.helmet(5), TestData.bicycle(5, 5));
    }

    @Test
    @DisplayName("вещи с разными номерами не равны")
    void differentNumbersAreNotEqual() {
        assertNotEquals(TestData.helmet(1), TestData.helmet(2));
    }

    @Test
    @DisplayName("вещь не равна null")
    void neverEqualsNull() {
        assertFalse(TestData.helmet(1).equals(null));
    }
}

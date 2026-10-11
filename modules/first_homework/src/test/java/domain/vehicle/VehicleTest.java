package domain.vehicle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import domain.exception.InvalidSimplicityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import support.TestData;

/**
 * Общее поведение транспорта: шкала простоты и равенство по номеру.
 *
 * <p>Vehicle абстрактный, поэтому проверяется через {@link Bicycle} —
 * самый простой наследник без собственных полей.
 */
@DisplayName("Vehicle")
class VehicleTest {

    @ParameterizedTest
    @ValueSource(ints = {Vehicle.MIN_SIMPLICITY, Vehicle.MAX_SIMPLICITY})
    @DisplayName("принимает границы шкалы простоты")
    void acceptsSimplicityBounds(int simplicity) {
        assertEquals(simplicity,
                TestData.bicycle(1, simplicity).getSimplicity());
    }

    @ParameterizedTest
    @ValueSource(ints = {Vehicle.MIN_SIMPLICITY - 1, Vehicle.MAX_SIMPLICITY + 1})
    @DisplayName("отклоняет простоту за границами шкалы")
    void rejectsSimplicityOutOfRange(int simplicity) {
        InvalidSimplicityException error = assertThrows(
                InvalidSimplicityException.class,
                () -> TestData.bicycle(1, simplicity));

        assertEquals(simplicity, error.getValue());
    }

    @Test
    @DisplayName("равенство определяется только инвентарным номером")
    void equalityByInventoryNumber() {
        Vehicle first = new Bicycle(7, "Stels", 4);
        Vehicle sameNumber = new Bicycle(7, "Другое название", 9);

        assertEquals(first, sameNumber);
        assertEquals(first.hashCode(), sameNumber.hashCode());
        assertNotEquals(first, new Bicycle(8, "Stels", 4));
    }

    @Test
    @DisplayName("не равен null и объектам других типов")
    void neverEqualsNullOrForeignType() {
        Vehicle vehicle = TestData.bicycle(1, 5);

        assertFalse(vehicle.equals(null));
        assertFalse(vehicle.equals("велосипед №1"));
    }

    @Test
    @DisplayName("транспорт разных видов с одним номером равен — это один объект учёта")
    void equalityIgnoresKindWithinVehicles() {
        assertEquals(TestData.bicycle(3, 5), TestData.scooter(3, 0.4));
    }
}

package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.inspection.InspectionResult;
import domain.vehicle.Vehicle;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.TestData;

/**
 * Итог приёма: транспорт есть тогда и только тогда, когда приём прошёл.
 */
@DisplayName("AcceptanceResult")
class AcceptanceResultTest {

    @Test
    @DisplayName("успешный приём содержит транспорт")
    void acceptedHasVehicle() {
        Vehicle vehicle = TestData.bicycle(1, 5);
        AcceptanceResult result =
                AcceptanceResult.accepted(InspectionResult.pass(), vehicle);

        assertTrue(result.isAccepted());
        assertEquals(Optional.of(vehicle), result.getVehicle());
    }

    @Test
    @DisplayName("отказ не содержит транспорта, но хранит причину")
    void rejectedHasNoVehicle() {
        AcceptanceResult result = AcceptanceResult.rejected(
                InspectionResult.fail("тормоза"));

        assertFalse(result.isAccepted());
        assertTrue(result.getVehicle().isEmpty());
        assertEquals("тормоза", result.getInspection().reason());
    }

    @Test
    @DisplayName("успешный приём без транспорта создать нельзя")
    void acceptedRequiresVehicle() {
        assertThrows(NullPointerException.class,
                () -> AcceptanceResult.accepted(InspectionResult.pass(), null));
    }

    @Test
    @DisplayName("отказ без решения техосмотра создать нельзя")
    void rejectedRequiresInspection() {
        assertThrows(NullPointerException.class,
                () -> AcceptanceResult.rejected(null));
    }
}

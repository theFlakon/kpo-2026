package domain.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import domain.exception.InvalidEnergyConsumptionException;
import domain.exception.InvalidNameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import support.TestData;

/**
 * Заявка на вещь проверяет себя до выдачи инвентарного номера.
 */
@DisplayName("ItemRequest")
class ItemRequestTest {

    @Test
    @DisplayName("расход по умолчанию равен нулю")
    void energyDefaultsToZero() {
        assertEquals(0.0, TestData.item().build().dailyEnergyKwh());
    }

    @Test
    @DisplayName("отклоняет пустое название")
    void rejectsBlankName() {
        assertThrows(InvalidNameException.class,
                () -> TestData.item().name(" ").build());
    }

    @Test
    @DisplayName("отклоняет отрицательный расход")
    void rejectsNegativeEnergy() {
        assertThrows(InvalidEnergyConsumptionException.class,
                () -> TestData.item().dailyEnergyKwh(-1).build());
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.NaN, Double.NEGATIVE_INFINITY})
    @DisplayName("отклоняет NaN и бесконечный расход")
    void rejectsNonFiniteEnergy(double kwh) {
        assertThrows(InvalidEnergyConsumptionException.class,
                () -> TestData.item().dailyEnergyKwh(kwh).build());
    }

    @Test
    @DisplayName("код вида не проверяется: это задача фабрики")
    void doesNotValidateTypeCode() {
        assertEquals("неизвестный",
                TestData.item().itemType("неизвестный").build().itemType());
    }
}

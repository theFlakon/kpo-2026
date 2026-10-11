package domain.inspection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import domain.exception.InvalidEnergyConsumptionException;
import domain.exception.InvalidNameException;
import domain.exception.InvalidSimplicityException;
import domain.exception.InvalidTechnicalConditionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import support.TestData;

/**
 * Заявка на приёмку: некорректную заявку нельзя создать.
 */
@DisplayName("IntakeRequest")
class IntakeRequestTest {

    @Test
    @DisplayName("расход по умолчанию равен нулю — для велосипеда поле можно не задавать")
    void energyDefaultsToZero() {
        IntakeRequest request = IntakeRequest.builder()
                .vehicleType(TestData.BICYCLE)
                .name("Stels")
                .simplicity(TestData.EASY)
                .technicalCondition(TestData.GOOD_CONDITION)
                .brakesOk(true)
                .build();

        assertEquals(0.0, request.dailyEnergyKwh());
    }

    @ParameterizedTest
    @ValueSource(ints = {IntakeRequest.MIN_CONDITION - 1,
            IntakeRequest.MAX_CONDITION + 1})
    @DisplayName("отклоняет состояние за пределами 0–100 %")
    void rejectsConditionOutOfRange(int condition) {
        assertThrows(InvalidTechnicalConditionException.class,
                () -> TestData.intake().technicalCondition(condition).build());
    }

    @Test
    @DisplayName("проверяет простоту теми же правилами, что и транспорт")
    void reusesVehicleSimplicityRule() {
        assertThrows(InvalidSimplicityException.class,
                () -> TestData.intake().simplicity(11).build());
    }

    @ParameterizedTest
    @ValueSource(ints = {IntakeRequest.MIN_CONDITION, IntakeRequest.MAX_CONDITION})
    @DisplayName("принимает границы диапазона состояния")
    void acceptsConditionBounds(int condition) {
        assertEquals(condition, TestData.intake().technicalCondition(condition)
                .build().technicalCondition());
    }

    @Test
    @DisplayName("отклоняет пустое название и отрицательный расход")
    void reusesNameAndEnergyRules() {
        assertThrows(InvalidNameException.class,
                () -> TestData.intake().name("").build());
        assertThrows(InvalidEnergyConsumptionException.class,
                () -> TestData.intake().dailyEnergyKwh(-0.5).build());
    }

    @Test
    @DisplayName("неизвестный вид в заявке допустим — его отсечёт фабрика")
    void doesNotValidateVehicleType() {
        assertEquals("segway",
                TestData.intake().vehicleType("segway").build().vehicleType());
    }
}

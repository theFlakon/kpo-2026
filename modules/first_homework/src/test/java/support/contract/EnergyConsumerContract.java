package support.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import domain.capability.EnergyConsumer;
import domain.exception.InvalidEnergyConsumptionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Контракт роли {@link EnergyConsumer}: общие тесты для всех
 * потребителей энергии — и транспорта, и вещей.
 *
 * <p>Используется так же, как {@link InventoriableContract}.
 */
public interface EnergyConsumerContract {

    /**
     * Создаёт проверяемый объект с заданным расходом.
     *
     * @param dailyEnergyKwh суточный расход
     * @return объект проверяемого вида
     */
    EnergyConsumer createWithEnergy(double dailyEnergyKwh);

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 0.4, 12.5})
    @DisplayName("EnergyConsumer: хранит суточный расход")
    default void keepsDailyEnergy(double kwh) {
        assertEquals(kwh, createWithEnergy(kwh).getDailyEnergyKwh());
    }

    // NaN проверяется отдельно от отрицательных: сравнение NaN < 0 ложно,
    // и именно этот случай легко пропустить в реализации.
    @ParameterizedTest
    @ValueSource(doubles = {-0.1, Double.NaN, Double.POSITIVE_INFINITY})
    @DisplayName("EnergyConsumer: отклоняет отрицательный, NaN и бесконечный расход")
    default void rejectsInvalidEnergy(double kwh) {
        assertThrows(InvalidEnergyConsumptionException.class,
                () -> createWithEnergy(kwh));
    }
}

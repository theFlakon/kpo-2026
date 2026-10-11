package cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.capability.Inventoriable;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.RuleBasedServiceCenter;
import support.FleetFixture;
import support.TestConsole;

/**
 * Демонстрация проходит все сценарии и не падает на ошибочных.
 */
@DisplayName("DemoScenario")
class DemoScenarioTest {

    @Test
    @DisplayName("ставит на баланс по одному объекту каждого вида")
    void registersOneOfEachType() {
        FleetFixture park = FleetFixture.with(new RuleBasedServiceCenter(
                RuleBasedServiceCenter.DEFAULT_MIN_CONDITION));
        TestConsole console = TestConsole.withInput();

        new DemoScenario(park.fleet(), park.reports(), console.printer()).run();

        // Отказы и ошибки данных в демонстрации ничего не добавляют.
        int expected = park.fleet().availableVehicleTypes().size()
                + park.fleet().availableItemTypes().size();
        assertEquals(expected, park.reports().countAll());
        assertTrue(console.output().contains("Отказ техосмотра"));
        assertTrue(console.output().contains("Ошибка: Неизвестный вид"));
    }

    @Test
    @DisplayName("отказы и ошибки не тратят номера: нумерация без дыр")
    void numbersHaveNoGaps() {
        FleetFixture park = FleetFixture.with(new RuleBasedServiceCenter(
                RuleBasedServiceCenter.DEFAULT_MIN_CONDITION));

        new DemoScenario(park.fleet(), park.reports(),
                TestConsole.withInput().printer()).run();

        List<Integer> numbers = park.reports().inventory().stream()
                .map(Inventoriable::getInventoryNumber).toList();
        assertEquals(IntStream.rangeClosed(1, numbers.size()).boxed().toList(),
                numbers);
    }

    @Test
    @DisplayName("отчёт для новичков показывает оба случая простоты")
    void beginnerReportShowsBothCases() {
        FleetFixture park = FleetFixture.with(new RuleBasedServiceCenter(
                RuleBasedServiceCenter.DEFAULT_MIN_CONDITION));

        new DemoScenario(park.fleet(), park.reports(),
                TestConsole.withInput().printer()).run();

        int beginners = park.reports().beginnerFriendlyVehicles().size();
        assertTrue(beginners > 0);
        assertTrue(beginners < park.reports().countVehicles());
    }
}

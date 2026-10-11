package cli;

import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.exception.UnknownTypeCodeException;
import domain.inspection.InspectionResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.AcceptanceResult;
import support.FleetFixture;
import support.StubServiceCenter;
import support.TestConsole;
import support.TestData;

/**
 * Форматирование вывода.
 */
@DisplayName("ConsolePrinter")
class ConsolePrinterTest {

    @Test
    @DisplayName("энергия печатается с двумя знаками и запятой")
    void formatsEnergyWithComma() {
        FleetFixture park = FleetFixture.with(StubServiceCenter.passing());
        park.repository().add(TestData.cabinet(1, 2.5));
        TestConsole console = TestConsole.withInput();

        console.printer().printEnergy(park.reports());

        assertTrue(console.output().contains("2,50 кВт·ч"));
    }

    @Test
    @DisplayName("пустая ведомость — понятное сообщение, а не пустота")
    void emptyInventoryHasMessage() {
        TestConsole console = TestConsole.withInput();

        console.printer().printInventory(
                FleetFixture.with(StubServiceCenter.passing()).reports());

        assertTrue(console.output().contains("ничего нет"));
    }

    @Test
    @DisplayName("отказ техосмотра печатается с причиной")
    void printsRejectionReason() {
        TestConsole console = TestConsole.withInput();

        console.printer().printAcceptance(AcceptanceResult.rejected(
                InspectionResult.fail("тормоза неисправны")));

        assertTrue(console.output().contains("Отказ техосмотра: тормоза"));
    }

    @Test
    @DisplayName("число единиц — всего и по веткам")
    void printsCounts() {
        FleetFixture park = FleetFixture.with(StubServiceCenter.passing());
        park.repository().add(TestData.bicycle(1, 5));
        park.repository().add(TestData.helmet(2));
        TestConsole console = TestConsole.withInput();

        console.printer().printCounts(park.reports());

        assertTrue(console.output().contains(
                "Всего на балансе: 2 (транспорт: 1, вещи: 1)"));
    }

    @Test
    @DisplayName("нет транспорта для новичков — понятное сообщение")
    void emptyBeginnerList() {
        FleetFixture park = FleetFixture.with(StubServiceCenter.passing());
        park.repository().add(TestData.bicycle(1, TestData.HARD));
        TestConsole console = TestConsole.withInput();

        console.printer().printBeginners(park.reports());

        assertTrue(console.output().contains("подходящего транспорта нет"));
        assertTrue(console.output().contains("простота ≥ 6"));
    }

    @Test
    @DisplayName("ведомость подписывает ветку учёта")
    void inventoryShowsCategory() {
        FleetFixture park = FleetFixture.with(StubServiceCenter.passing());
        park.repository().add(TestData.bicycle(1, 5));
        park.repository().add(TestData.helmet(2));
        TestConsole console = TestConsole.withInput();

        console.printer().printInventory(park.reports());

        assertTrue(console.output().contains("[транспорт]"));
        assertTrue(console.output().contains("[вещь]"));
    }

    @Test
    @DisplayName("успешный приём печатает номер и причину")
    void printsAcceptance() {
        TestConsole console = TestConsole.withInput();

        console.printer().printAcceptance(AcceptanceResult.accepted(
                InspectionResult.pass(), TestData.bicycle(7, 5)));

        assertTrue(console.output().contains("Принято: №7"));
        assertTrue(console.output().contains("Техосмотр пройден"));
    }

    @Test
    @DisplayName("ошибка печатается с сообщением исключения")
    void printsError() {
        TestConsole console = TestConsole.withInput();

        console.printer().printError(new UnknownTypeCodeException("segway"));

        assertTrue(console.output().contains("Ошибка: Неизвестный вид: 'segway'"));
    }

    @Test
    @DisplayName("меню перечисляет все пункты")
    void menuListsAllItems() {
        TestConsole console = TestConsole.withInput();

        console.printer().printMenu();

        for (int item = 0; item <= 7; item++) {
            assertTrue(console.output().contains("  " + item + ". "));
        }
    }
}

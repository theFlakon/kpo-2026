package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.capability.Inventoriable;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.InMemoryInventoryRepository;
import repository.InventoryRepository;
import support.TestData;

/**
 * Отчёты по парку. Хранилище заполняется напрямую, без FleetService:
 * отчёты проверяются отдельно от приёма.
 */
@DisplayName("ReportService")
class ReportServiceTest {

    /** Допуск при сравнении сумм в double. */
    private static final double DELTA = 1e-9;

    private InventoryRepository repository;

    private ReportService reports;

    @BeforeEach
    void setUp() {
        repository = new InMemoryInventoryRepository();
        reports = new ReportService(repository, new SimplicityThresholdPolicy(
                SimplicityThresholdPolicy.DEFAULT_THRESHOLD));
        repository.add(TestData.bicycle(1, TestData.HARD));
        repository.add(TestData.scooter(2, 0.4));
        repository.add(TestData.helmet(3));
        repository.add(TestData.cabinet(4, 2.5));
    }

    @Test
    @DisplayName("считает всё, транспорт и вещи")
    void countsByBranch() {
        assertEquals(4, reports.countAll());
        assertEquals(2, reports.countVehicles());
        assertEquals(2, reports.countItems());
    }

    @Test
    @DisplayName("энергия суммируется по всем потребителям, включая вещи")
    void sumsEnergyAcrossBranches() {
        // Самокат 0.4 + шкаф 2.5; велосипед и шлем не учитываются.
        assertEquals(2.9, reports.totalDailyEnergyKwh(), DELTA);
    }

    @Test
    @DisplayName("для новичков — только транспорт, прошедший правило")
    void selectsBeginnerFriendlyVehicles() {
        assertEquals(List.of(2), numbers(reports.beginnerFriendlyVehicles()));
    }

    @Test
    @DisplayName("правило для новичков подменяется, отчёт не меняется")
    void beginnerPolicyIsReplaceable() {
        ReportService onlyFirst = new ReportService(repository,
                vehicle -> vehicle.getInventoryNumber() == 1);

        assertEquals(List.of(1), numbers(onlyFirst.beginnerFriendlyVehicles()));
    }

    @Test
    @DisplayName("пустой парк — нулевые отчёты")
    void emptyParkGivesZeros() {
        ReportService empty = new ReportService(
                new InMemoryInventoryRepository(), vehicle -> true);

        assertEquals(0, empty.countAll());
        assertEquals(0.0, empty.totalDailyEnergyKwh(), DELTA);
    }

    private static List<Integer> numbers(List<? extends Inventoriable> list) {
        return list.stream().map(Inventoriable::getInventoryNumber).toList();
    }

    @Test
    @DisplayName("парк без потребителей — нулевой расход")
    void noConsumersGiveZeroEnergy() {
        InventoryRepository passive = new InMemoryInventoryRepository();
        passive.add(TestData.bicycle(1, 5));
        passive.add(TestData.helmet(2));

        assertEquals(0.0, new ReportService(passive, vehicle -> true)
                .totalDailyEnergyKwh(), DELTA);
    }

    @Test
    @DisplayName("вещи никогда не попадают в список для новичков")
    void itemsNeverInBeginnerList() {
        ReportService everything = new ReportService(repository, vehicle -> true);

        assertEquals(List.of(1, 2), numbers(everything.beginnerFriendlyVehicles()));
    }

    @Test
    @DisplayName("ведомость содержит всё в порядке постановки")
    void inventoryListsEverythingInOrder() {
        assertEquals(List.of(1, 2, 3, 4), numbers(reports.inventory()));
    }

    @Test
    @DisplayName("описание правила берётся из политики")
    void ruleDescriptionComesFromPolicy() {
        assertEquals("простота ≥ 6", reports.beginnerRuleDescription());
        assertTrue(new ReportService(repository, vehicle -> true)
                .beginnerRuleDescription().contains("правилу"));
    }
}

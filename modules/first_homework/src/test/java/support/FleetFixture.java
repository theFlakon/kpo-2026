package support;

import domain.inspection.ServiceCenter;
import repository.InMemoryInventoryRepository;
import repository.InventoryRepository;
import service.BeginnerPolicy;
import service.FleetService;
import service.InventoryNumberGenerator;
import service.ReportService;
import service.SimplicityThresholdPolicy;

/**
 * Собранный парк для тестов: сервисы поверх общего свежего хранилища.
 *
 * <p>Тест передаёт только то, что подменяет (техосмотр, правило для
 * новичков), остальное собирается как в приложении. Каждый вызов
 * создаёт новый парк, поэтому состояние между тестами не протекает.
 *
 * @param repository хранилище парка
 * @param fleet      сервис постановки на баланс
 * @param reports    сервис отчётов
 */
public record FleetFixture(InventoryRepository repository, FleetService fleet,
        ReportService reports) {

    /**
     * Парк с заданным техосмотром и правилом для новичков по умолчанию.
     *
     * @param serviceCenter подменный техосмотр
     * @return новый парк
     */
    public static FleetFixture with(ServiceCenter serviceCenter) {
        return with(serviceCenter, new SimplicityThresholdPolicy(
                SimplicityThresholdPolicy.DEFAULT_THRESHOLD));
    }

    /**
     * Парк с заданными техосмотром и правилом для новичков.
     *
     * @param serviceCenter  подменный техосмотр
     * @param beginnerPolicy подменное правило
     * @return новый парк
     */
    public static FleetFixture with(ServiceCenter serviceCenter,
            BeginnerPolicy beginnerPolicy) {
        InventoryRepository repository = new InMemoryInventoryRepository();
        FleetService fleet = new FleetService(serviceCenter,
                TestData.vehicleFactory(), TestData.itemFactory(),
                new InventoryNumberGenerator(), repository);
        return new FleetFixture(repository, fleet,
                new ReportService(repository, beginnerPolicy));
    }
}

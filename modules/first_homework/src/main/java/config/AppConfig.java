package config;

import cli.ConsoleApp;
import cli.ConsoleInput;
import cli.ConsolePrinter;
import cli.DemoScenario;
import domain.inspection.ServiceCenter;
import domain.item.ChargingCabinet;
import domain.item.DockingStation;
import domain.item.Helmet;
import domain.vehicle.Bicycle;
import domain.vehicle.ElectricBike;
import domain.vehicle.ElectricScooter;
import java.util.Scanner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import repository.InMemoryInventoryRepository;
import repository.InventoryRepository;
import service.BeginnerPolicy;
import service.FleetService;
import service.InventoryNumberGenerator;
import service.ItemFactory;
import service.ReportService;
import service.RuleBasedServiceCenter;
import service.SimplicityThresholdPolicy;
import service.VehicleFactory;

/**
 * Конфигурация DI-контейнера: весь граф зависимостей в одном месте.
 *
 * <p>Классы приложения не помечены аннотациями Spring — бины объявлены
 * здесь явно. Поэтому домен и сервисы не зависят от фреймворка,
 * а граф виден целиком.
 *
 * <p>Все бины — синглтоны (поведение Spring по умолчанию). Для генератора
 * номеров и репозитория это критично: второй генератор начал бы выдавать
 * повторяющиеся номера, а второй репозиторий разделил бы парк надвое.
 *
 * <p>Бины, у которых есть абстракция, объявлены типом интерфейса:
 * остальной граф знает только её, и тест подменяет именно её.
 */
@Configuration(proxyBeanMethods = false)
public final class AppConfig {

    /** Код вида «электросамокат». */
    private static final String SCOOTER = "scooter";

    /** Код вида «велосипед». */
    private static final String BICYCLE = "bicycle";

    /** Код вида «электровелосипед». */
    private static final String E_BIKE = "ebike";

    /** Код вида «шлем». */
    private static final String HELMET = "helmet";

    /** Код вида «док-станция». */
    private static final String DOCK = "dock";

    /** Код вида «зарядный шкаф». */
    private static final String CABINET = "cabinet";

    /**
     * Техосмотр с порогом состояния по умолчанию.
     *
     * @return реализация техосмотра
     */
    @Bean
    public ServiceCenter serviceCenter() {
        return new RuleBasedServiceCenter(
                RuleBasedServiceCenter.DEFAULT_MIN_CONDITION);
    }

    /**
     * Правило для новичков из задания: простота от 6.
     *
     * @return правило отбора транспорта
     */
    @Bean
    public BeginnerPolicy beginnerPolicy() {
        return new SimplicityThresholdPolicy(
                SimplicityThresholdPolicy.DEFAULT_THRESHOLD);
    }

    /**
     * Хранилище парка в памяти.
     *
     * @return единственное хранилище приложения
     */
    @Bean
    public InventoryRepository inventoryRepository() {
        return new InMemoryInventoryRepository();
    }

    /**
     * Единый источник инвентарных номеров для транспорта и вещей.
     *
     * @return единственный генератор приложения
     */
    @Bean
    public InventoryNumberGenerator inventoryNumberGenerator() {
        return new InventoryNumberGenerator();
    }

    /**
     * Реестр видов транспорта.
     *
     * <p>Новый вид добавляется здесь одной регистрацией; порядок
     * регистрации — порядок в меню консоли.
     *
     * @return фабрика с зарегистрированными видами
     */
    @Bean
    public VehicleFactory vehicleFactory() {
        VehicleFactory factory = new VehicleFactory();
        factory.register(SCOOTER, "Электросамокат", ElectricScooter.class,
                (number, r) -> new ElectricScooter(number, r.name(),
                        r.simplicity(), r.dailyEnergyKwh()));
        factory.register(BICYCLE, "Велосипед", Bicycle.class,
                (number, r) -> new Bicycle(number, r.name(), r.simplicity()));
        factory.register(E_BIKE, "Электровелосипед", ElectricBike.class,
                (number, r) -> new ElectricBike(number, r.name(),
                        r.simplicity(), r.dailyEnergyKwh()));
        return factory;
    }

    /**
     * Реестр видов вещей.
     *
     * @return фабрика с зарегистрированными видами
     */
    @Bean
    public ItemFactory itemFactory() {
        ItemFactory factory = new ItemFactory();
        factory.register(HELMET, "Шлем", Helmet.class,
                (number, r) -> new Helmet(number, r.name()));
        factory.register(DOCK, "Док-станция", DockingStation.class,
                (number, r) -> new DockingStation(number, r.name(),
                        r.dailyEnergyKwh()));
        factory.register(CABINET, "Зарядный шкаф", ChargingCabinet.class,
                (number, r) -> new ChargingCabinet(number, r.name(),
                        r.dailyEnergyKwh()));
        return factory;
    }

    /**
     * Сервис постановки на баланс.
     *
     * @param serviceCenter    техосмотр
     * @param vehicleFactory   реестр видов транспорта
     * @param itemFactory      реестр видов вещей
     * @param numberGenerator  источник инвентарных номеров
     * @param repository       хранилище парка
     * @return сервис приёма
     */
    @Bean
    public FleetService fleetService(ServiceCenter serviceCenter,
                                     VehicleFactory vehicleFactory, ItemFactory itemFactory,
                                     InventoryNumberGenerator numberGenerator,
                                     InventoryRepository repository) {
        // Порядок аргументов совпадает с порядком полей FleetService:
        // конструктор сгенерирован Lombok по объявлению полей.
        return new FleetService(serviceCenter, vehicleFactory, itemFactory,
                numberGenerator, repository);
    }

    /**
     * Сервис отчётов.
     *
     * @param repository     хранилище парка
     * @param beginnerPolicy правило для новичков
     * @return сервис отчётов
     */
    @Bean
    public ReportService reportService(InventoryRepository repository,
                                       BeginnerPolicy beginnerPolicy) {
        return new ReportService(repository, beginnerPolicy);
    }

    /**
     * Вывод в стандартный поток.
     *
     * @return форматирование вывода
     */
    @Bean
    public ConsolePrinter consolePrinter() {
        return new ConsolePrinter(System.out);
    }

    /**
     * Ввод из стандартного потока.
     *
     * @return чтение ввода
     */
    @Bean
    public ConsoleInput consoleInput() {
        return new ConsoleInput(new Scanner(System.in), System.out);
    }

    /**
     * Демонстрация всех сценариев.
     *
     * @param fleet   сервис приёма
     * @param reports сервис отчётов
     * @param printer форматирование вывода
     * @return сценарий демонстрации
     */
    @Bean
    public DemoScenario demoScenario(FleetService fleet,
                                     ReportService reports, ConsolePrinter printer) {
        return new DemoScenario(fleet, reports, printer);
    }

    /**
     * Консольное приложение.
     *
     * @param fleet   сервис приёма
     * @param reports сервис отчётов
     * @param input   чтение ввода
     * @param printer форматирование вывода
     * @param demo    сценарий демонстрации
     * @return консольное приложение
     */
    @Bean
    public ConsoleApp consoleApp(FleetService fleet, ReportService reports,
                                 ConsoleInput input, ConsolePrinter printer, DemoScenario demo) {
        return new ConsoleApp(fleet, reports, input, printer, demo);
    }
}
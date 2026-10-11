package service;

import domain.capability.EnergyConsumer;
import domain.capability.Inventoriable;
import domain.item.Item;
import domain.vehicle.Vehicle;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import repository.InventoryRepository;

/**
 * Отчёты по парку. Только чтение: состояние парка не меняется.
 *
 * <p>Возвращает данные, а не строки: форматирование — забота консоли.
 * Поэтому отчёты проверяются в тестах без разбора текста.
 */
@RequiredArgsConstructor
public final class ReportService {

    /** Хранилище объектов на балансе. */
    @NonNull
    private final InventoryRepository repository;

    /** Правило отбора транспорта для новичков. */
    @NonNull
    private final BeginnerPolicy beginnerPolicy;

    /**
     * Считает все объекты на балансе.
     *
     * @return число единиц транспорта и вещей
     */
    public int countAll() {
        return repository.findAll().size();
    }

    /**
     * Считает транспорт на балансе.
     *
     * @return число единиц транспорта
     */
    public int countVehicles() {
        return repository.findAllOf(Vehicle.class).size();
    }

    /**
     * Считает вещи на балансе.
     *
     * @return число вещей
     */
    public int countItems() {
        return repository.findAllOf(Item.class).size();
    }

    /**
     * Суммирует суточный расход энергии всего парка.
     *
     * <p>Учитывает всех потребителей независимо от ветки: и электротранспорт,
     * и док-станции с зарядными шкафами.
     *
     * @return суммарный расход, кВт·ч в сутки
     */
    public double totalDailyEnergyKwh() {
        // DoubleStream.sum() использует компенсированное суммирование,
        // поэтому погрешность меньше, чем у наивного цикла с +=.
        return repository.findAllOf(EnergyConsumer.class).stream()
                .mapToDouble(EnergyConsumer::getDailyEnergyKwh)
                .sum();
    }

    /**
     * Отбирает транспорт, который можно выдавать новичкам.
     *
     * @return неизменяемый список в порядке постановки на баланс
     */
    public List<Vehicle> beginnerFriendlyVehicles() {
        return repository.findAllOf(Vehicle.class).stream()
                .filter(beginnerPolicy::isSuitableForBeginner)
                .toList();
    }

    /**
     * Возвращает все объекты на балансе для вывода названий и номеров.
     *
     * @return неизменяемый список в порядке постановки на баланс
     */
    public List<Inventoriable> inventory() {
        return repository.findAll();
    }

    /**
     * Описывает правило отбора транспорта для новичков.
     *
     * @return краткое описание, например «простота ≥ 6»
     */
    public String beginnerRuleDescription() {
        return beginnerPolicy.description();
    }
}
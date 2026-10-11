package service;

import domain.inspection.InspectionResult;
import domain.inspection.IntakeRequest;
import domain.inspection.ServiceCenter;
import domain.item.Item;
import domain.item.ItemRequest;
import domain.vehicle.Vehicle;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import repository.InventoryRepository;

/**
 * Постановка на баланс: приём техники и регистрация вещей.
 *
 * <p>Зависимостей пять, но ответственность одна: каждая зависимость
 * отвечает за свой шаг постановки на баланс. Техосмотр приходит
 * как абстракция {@link ServiceCenter}, поэтому его подменяют через DI,
 * а сервис об этом не знает.
 *
 * <p>Порядок шагов выбран так, чтобы инвентарный номер выдавался
 * только объекту, который точно попадёт на баланс.
 */
// Lombok создаёт конструктор по final-полям в порядке их объявления
// и проверяет аргументы на null (поля помечены @NonNull).
@RequiredArgsConstructor
public final class FleetService {

    /** Техосмотр, решающий судьбу новой техники. */
    @NonNull
    private final ServiceCenter serviceCenter;

    /** Реестр и создание видов транспорта. */
    @NonNull
    private final VehicleFactory vehicleFactory;

    /** Реестр и создание видов вещей. */
    @NonNull
    private final ItemFactory itemFactory;

    /** Источник инвентарных номеров. */
    @NonNull
    private final InventoryNumberGenerator numberGenerator;

    /** Хранилище объектов на балансе. */
    @NonNull
    private final InventoryRepository repository;

    /**
     * Принимает технику в парк через техосмотр.
     *
     * <p>Шаги: проверка вида, техосмотр, выдача номера, создание,
     * сохранение. При отказе техосмотра номер не выдаётся и парк
     * не меняется.
     *
     * @param request корректная заявка на приёмку
     * @return итог приёма с решением техосмотра
     * @throws domain.exception.UnknownTypeCodeException
     *         если вид транспорта не зарегистрирован
     */
    public AcceptanceResult acceptVehicle(@NonNull IntakeRequest request) {
        // Вид проверяется первым: иначе техника неизвестного вида прошла бы
        // осмотр и упала бы только на создании.
        vehicleFactory.requireSupported(request.vehicleType());

        InspectionResult inspection = serviceCenter.inspect(request);
        if (!inspection.passed()) {
            return AcceptanceResult.rejected(inspection);
        }

        Vehicle vehicle =
                vehicleFactory.create(numberGenerator.next(), request);
        repository.add(vehicle);
        return AcceptanceResult.accepted(inspection, vehicle);
    }

    /**
     * Ставит вещь на баланс без техосмотра.
     *
     * @param request корректная заявка на вещь
     * @return вещь, поставленная на баланс
     * @throws domain.exception.UnknownTypeCodeException
     *         если вид вещи не зарегистрирован
     */
    public Item registerItem(@NonNull ItemRequest request) {
        itemFactory.requireSupported(request.itemType());
        Item item = itemFactory.create(numberGenerator.next(), request);
        repository.add(item);
        return item;
    }

    /**
     * Возвращает виды транспорта, которые можно принять.
     *
     * <p>Консоль строит из этого списка меню, поэтому новый вид
     * появляется в нём без правки консоли.
     *
     * @return неизменяемый список в порядке регистрации
     */
    public List<TypeDescriptor> availableVehicleTypes() {
        return vehicleFactory.availableTypes();
    }

    /**
     * Возвращает виды вещей, которые можно поставить на баланс.
     *
     * @return неизменяемый список в порядке регистрации
     */
    public List<TypeDescriptor> availableItemTypes() {
        return itemFactory.availableTypes();
    }
}
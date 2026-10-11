package service;

import domain.capability.EnergyConsumer;
import domain.exception.DuplicateTypeCodeException;
import domain.exception.UnknownTypeCodeException;
import domain.inspection.IntakeRequest;
import domain.vehicle.Vehicle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

/**
 * Реестр видов транспорта и создание транспорта по заявке.
 *
 * <p>Новый вид добавляется наследником {@link Vehicle} и одной строкой
 * регистрации в {@code AppConfig}; фабрика, сервисы и консоль при этом
 * не меняются.
 *
 * <p>Похожа на {@link ItemFactory}, но общий реестр сознательно
 * не выделен: по «правилу трёх» обобщение оправдано, когда появится
 * третья такая фабрика (например, для склада запчастей).
 */
public final class VehicleFactory {

    /**
     * Способ создать транспорт конкретного вида.
     *
     * @param <T> создаваемый вид транспорта
     */
    @FunctionalInterface
    public interface VehicleCreator<T extends Vehicle> {

        /**
         * Создаёт транспорт по одобренной заявке.
         *
         * @param inventoryNumber выданный инвентарный номер
         * @param request         корректная заявка
         * @return новый объект транспорта
         */
        T create(int inventoryNumber, IntakeRequest request);
    }

    /**
     * Запись реестра: описание вида и способ создания.
     *
     * @param descriptor описание вида для консоли
     * @param creator    способ создания
     */
    private record Registration(
            TypeDescriptor descriptor,
            VehicleCreator<? extends Vehicle> creator) {
    }

    // LinkedHashMap сохраняет порядок регистрации — в нём же консоль
    // покажет виды в меню.
    /** Зарегистрированные виды по коду. */
    private final Map<String, Registration> registry = new LinkedHashMap<>();

    /**
     * Регистрирует вид транспорта.
     *
     * <p>Класс вида и способ создания связаны одним параметром типа,
     * поэтому компилятор не даст зарегистрировать, например,
     * {@code ElectricScooter.class} с созданием велосипеда. По классу же
     * определяется, потребляет ли вид энергию: вручную этот признак
     * не задаётся и не может разойтись с реальным классом.
     *
     * @param code        уникальный код вида
     * @param displayName название для пользователя
     * @param type        класс вида
     * @param creator     способ создания
     * @param <T>         регистрируемый вид транспорта
     * @throws DuplicateTypeCodeException если код уже зарегистрирован
     */
    public <T extends Vehicle> void register(
            @NonNull String code,
            @NonNull String displayName,
            @NonNull Class<T> type,
            @NonNull VehicleCreator<T> creator) {
        TypeDescriptor descriptor = new TypeDescriptor(code, displayName,
                EnergyConsumer.class.isAssignableFrom(type));
        if (registry.putIfAbsent(code, new Registration(descriptor, creator))
                != null) {
            throw new DuplicateTypeCodeException(code);
        }
    }

    /**
     * Проверяет, что вид зарегистрирован.
     *
     * <p>Вызывается до техосмотра: иначе техника с неизвестным видом
     * прошла бы осмотр и упала бы только на создании.
     *
     * @param code проверяемый код, допускается {@code null}
     * @throws UnknownTypeCodeException если вид не зарегистрирован
     */
    public void requireSupported(String code) {
        lookup(code);
    }

    /**
     * Создаёт транспорт по заявке.
     *
     * @param inventoryNumber выданный инвентарный номер
     * @param request         корректная одобренная заявка
     * @return новый объект транспорта
     * @throws UnknownTypeCodeException если вид из заявки не зарегистрирован
     */
    public Vehicle create(int inventoryNumber,
                          @NonNull IntakeRequest request) {
        return lookup(request.vehicleType())
                .creator()
                .create(inventoryNumber, request);
    }

    /**
     * Возвращает описания всех зарегистрированных видов.
     *
     * @return неизменяемый список в порядке регистрации
     */
    public List<TypeDescriptor> availableTypes() {
        return registry.values().stream()
                .map(Registration::descriptor)
                .toList();
    }

    /**
     * Находит запись реестра по коду.
     *
     * @param code код вида, допускается {@code null}
     * @return запись реестра
     * @throws UnknownTypeCodeException если вид не зарегистрирован
     */
    private Registration lookup(String code) {
        // null-код обрабатывается как неизвестный: он приходит из заявки,
        // то есть из пользовательских данных, а не из кода программы.
        Registration registration = registry.get(code);
        if (registration == null) {
            throw new UnknownTypeCodeException(code);
        }
        return registration;
    }
}
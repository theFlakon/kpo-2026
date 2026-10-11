package service;

import domain.capability.EnergyConsumer;
import domain.exception.DuplicateTypeCodeException;
import domain.exception.UnknownTypeCodeException;
import domain.item.Item;
import domain.item.ItemRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

/**
 * Реестр видов вещей и создание вещей по заявке.
 *
 * <p>Новый вид добавляется наследником {@link Item} и одной строкой
 * регистрации в {@code AppConfig}; фабрика, сервисы и консоль при этом
 * не меняются.
 *
 * <p>Повторяет устройство {@link VehicleFactory}; почему общий реестр
 * не выделен, объяснено там.
 */
public final class ItemFactory {

    /**
     * Способ создать вещь конкретного вида.
     *
     * @param <T> создаваемый вид вещи
     */
    @FunctionalInterface
    public interface ItemCreator<T extends Item> {

        /**
         * Создаёт вещь по заявке.
         *
         * @param inventoryNumber выданный инвентарный номер
         * @param request         корректная заявка
         * @return новая вещь
         */
        T create(int inventoryNumber, ItemRequest request);
    }

    /**
     * Запись реестра: описание вида и способ создания.
     *
     * @param descriptor описание вида для консоли
     * @param creator    способ создания
     */
    private record Registration(
            TypeDescriptor descriptor,
            ItemCreator<? extends Item> creator) {
    }

    // LinkedHashMap сохраняет порядок регистрации — в нём же консоль
    // покажет виды в меню.
    /** Зарегистрированные виды по коду. */
    private final Map<String, Registration> registry = new LinkedHashMap<>();

    /**
     * Регистрирует вид вещи.
     *
     * <p>Класс вида и способ создания связаны одним параметром типа,
     * поэтому компилятор не даст зарегистрировать, например,
     * {@code ChargingCabinet.class} с созданием шлема. По классу же
     * определяется, потребляет ли вид энергию: вручную этот признак
     * не задаётся и не может разойтись с реальным классом.
     *
     * @param code        уникальный код вида
     * @param displayName название для пользователя
     * @param type        класс вида
     * @param creator     способ создания
     * @param <T>         регистрируемый вид вещи
     * @throws DuplicateTypeCodeException если код уже зарегистрирован
     */
    public <T extends Item> void register(
            @NonNull String code,
            @NonNull String displayName,
            @NonNull Class<T> type,
            @NonNull ItemCreator<T> creator) {
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
     * <p>Вызывается до выдачи инвентарного номера: иначе номер
     * был бы потрачен на вещь, которую нельзя создать.
     *
     * @param code проверяемый код, допускается {@code null}
     * @throws UnknownTypeCodeException если вид не зарегистрирован
     */
    public void requireSupported(String code) {
        lookup(code);
    }

    /**
     * Создаёт вещь по заявке.
     *
     * @param inventoryNumber выданный инвентарный номер
     * @param request         корректная заявка
     * @return новая вещь
     * @throws UnknownTypeCodeException если вид из заявки не зарегистрирован
     */
    public Item create(int inventoryNumber,
                       @NonNull ItemRequest request) {
        return lookup(request.itemType())
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
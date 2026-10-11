package repository;

import domain.capability.Inventoriable;
import domain.exception.DuplicateInventoryNumberException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

/**
 * Хранилище в памяти процесса.
 *
 * <p>Данные живут, пока работает приложение. В DI-контейнере
 * регистрируется как singleton; в тестах каждый тест создаёт
 * свой экземпляр, поэтому состояние между тестами не протекает.
 *
 * <p>Не потокобезопасно: консольное приложение однопоточное,
 * синхронизация была бы лишней.
 */
public final class InMemoryInventoryRepository
        implements InventoryRepository {

    /** Объекты на балансе по инвентарному номеру. */
    private final Map<Integer, Inventoriable> storage =
            new LinkedHashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(@NonNull Inventoriable entry) {
        int number = entry.getInventoryNumber();
        if (storage.putIfAbsent(number, entry) != null) {
            throw new DuplicateInventoryNumberException(number);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Inventoriable> findAll() {
        return List.copyOf(storage.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<T> findAllOf(@NonNull Class<T> type) {
        return storage.values().stream()
                .filter(type::isInstance)
                .map(type::cast)
                .toList();
    }
}
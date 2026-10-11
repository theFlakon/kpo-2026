package repository;

import domain.capability.Inventoriable;
import java.util.List;

/**
 * Хранилище объектов на балансе.
 *
 * <p>Сервисы зависят от этого интерфейса, а не от реализации: хранилище —
 * самая изменчивая зависимость, и переход на базу данных не должен
 * затрагивать сервисы.
 *
 * <p>Поиска по номеру и удаления нет сознательно: ни одно требование
 * их не использует.
 */
public interface InventoryRepository {

    /**
     * Ставит объект на баланс.
     *
     * @param entry объект с уникальным инвентарным номером
     * @throws domain.exception.DuplicateInventoryNumberException
     *         если объект с таким номером уже есть
     * @throws NullPointerException если {@code entry} равен {@code null}
     */
    void add(Inventoriable entry);

    /**
     * Возвращает все объекты на балансе.
     *
     * @return неизменяемый список в порядке добавления
     */
    List<Inventoriable> findAll();

    /**
     * Возвращает объекты, относящиеся к указанному типу или роли.
     *
     * <p>Одна обобщённая выборка вместо отдельного метода на каждую роль:
     * новая роль или вид техники не требуют правки репозитория.
     * Пример: {@code findAllOf(EnergyConsumer.class)}.
     *
     * @param type класс или интерфейс для отбора
     * @param <T>  тип элементов результата
     * @return неизменяемый список в порядке добавления
     * @throws NullPointerException если {@code type} равен {@code null}
     */
    <T> List<T> findAllOf(Class<T> type);
}
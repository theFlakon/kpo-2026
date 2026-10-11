package domain.item;

import domain.capability.Inventoriable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Базовый класс вещей на балансе.
 *
 * <p>Реализует {@link Inventoriable} напрямую: общего предка с транспортом
 * нет, поэтому номер и название хранятся здесь, а их проверка вынесена
 * в статические методы интерфейса и общая с {@code Vehicle}.
 *
 * <p>Класс отделяет вещи от транспорта на уровне типов: шлем не может
 * попасть в список транспорта для новичков, а {@code ItemFactory}
 * возвращает типизированный результат.
 *
 * <p>Объекты неизменяемы; корректность полей проверяется в конструкторе.
 */
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class Item implements Inventoriable {

    /** Инвентарный номер, выданный при постановке на баланс. */
    @EqualsAndHashCode.Include
    private final int inventoryNumber;

    /** Название, например «Шлем детский». */
    private final String name;

    /**
     * Создаёт вещь с проверкой инвариантов.
     *
     * @param inventoryNumber положительный инвентарный номер
     * @param name            непустое название
     * @throws domain.exception.InvalidInventoryNumberException
     *         если номер не положителен
     * @throws domain.exception.InvalidNameException
     *         если название пустое
     */
    protected Item(int inventoryNumber, String name) {
        this.inventoryNumber =
                Inventoriable.requireValidNumber(inventoryNumber);
        this.name = Inventoriable.requireValidName(name);
    }
}
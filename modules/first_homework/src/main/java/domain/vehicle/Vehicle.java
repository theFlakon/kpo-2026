package domain.vehicle;

import domain.capability.Inventoriable;
import domain.exception.InvalidSimplicityException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Базовый класс прокатного транспорта.
 *
 * <p>Хранит то, что есть у любого транспорта: инвентарный номер, название
 * и простоту для новичка. Энергопотребление сюда намеренно не входит:
 * его добавляют только те наследники, которые реализуют
 * {@link domain.capability.EnergyConsumer}.
 *
 * <p>Простота сделана полем, а не отдельным интерфейсом: кроме транспорта
 * её носителей нет, и интерфейс был бы декоративным.
 *
 * <p>Объекты неизменяемы; корректность полей проверяется в конструкторе.
 */
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class Vehicle implements Inventoriable {

    /** Минимально допустимое значение простоты. */
    public static final int MIN_SIMPLICITY = 1;

    /** Максимально допустимое значение простоты. */
    public static final int MAX_SIMPLICITY = 10;

    /** Инвентарный номер, выданный при приёмке в парк. */
    @EqualsAndHashCode.Include
    private final int inventoryNumber;

    /** Название модели, например «Ninebot Max G30». */
    private final String name;

    /** Простота для новичка по шкале от 1 до 10. */
    private final int simplicity;

    /**
     * Создаёт транспорт с проверкой всех инвариантов.
     *
     * @param inventoryNumber положительный инвентарный номер
     * @param name            непустое название модели
     * @param simplicity      простота для новичка, от {@value #MIN_SIMPLICITY}
     *                        до {@value #MAX_SIMPLICITY}
     * @throws domain.exception.InvalidInventoryNumberException
     *         если номер не положителен
     * @throws domain.exception.InvalidNameException
     *         если название пустое
     * @throws InvalidSimplicityException
     *         если простота вне диапазона
     */
    protected Vehicle(int inventoryNumber, String name, int simplicity) {
        this.inventoryNumber =
                Inventoriable.requireValidNumber(inventoryNumber);
        this.name = Inventoriable.requireValidName(name);
        this.simplicity = requireValidSimplicity(simplicity);
    }

    /**
     * Проверяет, что простота лежит в допустимом диапазоне.
     *
     * <p>Публичный, потому что то же правило проверяет заявка
     * на приёмку ещё до создания объекта.
     *
     * @param simplicity проверяемое значение
     * @return то же значение, если оно корректно
     * @throws InvalidSimplicityException если значение вне диапазона
     */
    public static int requireValidSimplicity(int simplicity) {
        if (simplicity < MIN_SIMPLICITY || simplicity > MAX_SIMPLICITY) {
            throw new InvalidSimplicityException(
                    simplicity, MIN_SIMPLICITY, MAX_SIMPLICITY);
        }
        return simplicity;
    }
}
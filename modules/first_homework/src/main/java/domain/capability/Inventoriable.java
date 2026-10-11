package domain.capability;

import domain.exception.InvalidInventoryNumberException;
import domain.exception.InvalidNameException;

/**
 * Объект, который стоит на балансе «ВышКат» и имеет инвентарный номер.
 *
 * <p>Интерфейс описывает роль, а не место в иерархии: его реализуют
 * и транспорт, и вещи (шлемы, док-станции, зарядные шкафы). Сотрудники
 * его не реализуют, поэтому в отчёт по балансу не попадают.
 *
 * <p>Контракт для реализаций:
 * <ul>
 *   <li>инвентарный номер положителен и уникален в пределах парка;</li>
 *   <li>номер не меняется за всё время жизни объекта;</li>
 *   <li>название не {@code null} и не пустое.</li>
 * </ul>
 */
public interface Inventoriable {

    /**
     * Возвращает инвентарный номер объекта.
     *
     * <p>Номер выдаётся один раз при постановке на баланс
     * (см. {@code InventoryNumberGenerator}) и далее не меняется.
     *
     * @return положительный уникальный инвентарный номер
     */
    int getInventoryNumber();

    /**
     * Возвращает человекочитаемое название объекта.
     *
     * @return непустое название, например «Электросамокат Ninebot»
     */
    String getName();

    /**
     * Проверяет инвентарный номер на соответствие контракту.
     *
     * <p>Проверка живёт в интерфейсе, а не в базовом классе: у транспорта
     * и вещей нет общего предка, а контракт у них один.
     *
     * @param inventoryNumber проверяемый номер
     * @return тот же номер, если он корректен
     * @throws InvalidInventoryNumberException если номер не положителен
     */
    static int requireValidNumber(int inventoryNumber) {
        if (inventoryNumber <= 0) {
            throw new InvalidInventoryNumberException(inventoryNumber);
        }
        return inventoryNumber;
    }

    /**
     * Проверяет название на соответствие контракту.
     *
     * @param name проверяемое название
     * @return то же название, если оно корректно
     * @throws InvalidNameException если название {@code null}
     *                              или состоит только из пробелов
     */
    static String requireValidName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidNameException(name);
        }
        return name;
    }
}
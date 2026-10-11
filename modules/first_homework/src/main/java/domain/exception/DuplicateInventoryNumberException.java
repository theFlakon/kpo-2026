package domain.exception;

import java.io.Serial;
import lombok.Getter;

/**
 * Инвентарный номер уже занят другим объектом на балансе.
 *
 * <p>Номер уникален в пределах всего парка, а не внутри ветки
 * транспорта или вещей: самокат №5 и шлем №5 одновременно
 * существовать не могут.
 */
@Getter
public final class DuplicateInventoryNumberException
        extends VyshKatException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /** Номер, который уже занят. */
    private final int inventoryNumber;

    /**
     * Создаёт исключение для занятого номера.
     *
     * @param inventoryNumber номер, который уже занят
     */
    public DuplicateInventoryNumberException(int inventoryNumber) {
        super("Инвентарный номер уже занят: " + inventoryNumber);
        this.inventoryNumber = inventoryNumber;
    }
}
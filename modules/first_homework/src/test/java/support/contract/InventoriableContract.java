package support.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import domain.capability.Inventoriable;
import domain.exception.InvalidInventoryNumberException;
import domain.exception.InvalidNameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Контракт роли {@link Inventoriable}: общие тесты для всех, кто стоит
 * на балансе.
 *
 * <p>Тестовый класс конкретного вида реализует этот интерфейс и метод
 * {@link #create} — и получает все тесты контракта. Новый вид техники
 * или вещи проверяется на контракт одной строкой {@code implements}.
 */
public interface InventoriableContract {

    /**
     * Создаёт проверяемый объект.
     *
     * @param inventoryNumber инвентарный номер
     * @param name            название
     * @return объект проверяемого вида
     */
    Inventoriable create(int inventoryNumber, String name);

    @Test
    @DisplayName("Inventoriable: хранит номер и название")
    default void keepsNumberAndName() {
        Inventoriable entry = create(42, "Объект");

        assertEquals(42, entry.getInventoryNumber());
        assertEquals("Объект", entry.getName());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    @DisplayName("Inventoriable: отклоняет неположительный номер")
    default void rejectsNonPositiveNumber(int number) {
        assertThrows(InvalidInventoryNumberException.class,
                () -> create(number, "Объект"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t"})
    @DisplayName("Inventoriable: отклоняет пустое название")
    default void rejectsBlankName(String name) {
        assertThrows(InvalidNameException.class, () -> create(1, name));
    }

    @Test
    @DisplayName("Inventoriable: отклоняет null вместо названия")
    default void rejectsNullName() {
        assertThrows(InvalidNameException.class, () -> create(1, null));
    }
}

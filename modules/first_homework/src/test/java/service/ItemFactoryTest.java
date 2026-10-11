package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import domain.exception.DuplicateTypeCodeException;
import domain.exception.UnknownTypeCodeException;
import domain.item.Helmet;
import domain.item.Item;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.TestData;

/**
 * Реестр видов вещей. Механизм общий с {@link VehicleFactory}, поэтому
 * здесь только ключевые проверки; подробности — в VehicleFactoryTest.
 */
@DisplayName("ItemFactory")
class ItemFactoryTest {

    private final ItemFactory factory = TestData.itemFactory();

    @Test
    @DisplayName("создаёт вещь нужного вида с выданным номером")
    void createsRegisteredType() {
        Item item = factory.create(3, TestData.item().build());

        assertInstanceOf(Helmet.class, item);
        assertEquals(3, item.getInventoryNumber());
    }

    @Test
    @DisplayName("неизвестный вид отклоняется")
    void rejectsUnknownType() {
        assertThrows(UnknownTypeCodeException.class,
                () -> factory.create(1, TestData.item().itemType("drone")
                        .build()));
    }

    @Test
    @DisplayName("признак энергопотребления вычисляется по классу вида")
    void derivesEnergyFlagFromClass() {
        assertEquals(List.of(false, true, true), factory.availableTypes()
                .stream().map(TypeDescriptor::consumesEnergy).toList());
    }

    @Test
    @DisplayName("повторный код отклоняется")
    void rejectsDuplicateCode() {
        assertThrows(DuplicateTypeCodeException.class,
                () -> factory.register(TestData.HELMET, "Ещё шлем",
                        Helmet.class, (n, r) -> new Helmet(n, r.name())));
    }
}

package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import domain.exception.DuplicateTypeCodeException;
import domain.exception.UnknownTypeCodeException;
import domain.vehicle.Bicycle;
import domain.vehicle.ElectricScooter;
import domain.vehicle.Vehicle;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.TestData;

/**
 * Реестр видов транспорта.
 *
 * <p>Фабрика собирается здесь своя, а не из AppConfig: тест проверяет
 * механизм регистрации, а не конкретный набор видов приложения.
 */
@DisplayName("VehicleFactory")
class VehicleFactoryTest {

    private VehicleFactory factory;

    @BeforeEach
    void setUp() {
        factory = new VehicleFactory();
        factory.register("scooter", "Электросамокат", ElectricScooter.class,
                (n, r) -> new ElectricScooter(n, r.name(), r.simplicity(),
                        r.dailyEnergyKwh()));
        factory.register("bicycle", "Велосипед", Bicycle.class,
                (n, r) -> new Bicycle(n, r.name(), r.simplicity()));
    }

    @Test
    @DisplayName("создаёт транспорт нужного вида с выданным номером")
    void createsRegisteredType() {
        Vehicle vehicle = factory.create(7, TestData.intake().build());

        assertInstanceOf(ElectricScooter.class, vehicle);
        assertEquals(7, vehicle.getInventoryNumber());
    }

    @Test
    @DisplayName("вычисляет признак энергопотребления по классу вида")
    void derivesEnergyFlagFromClass() {
        List<Boolean> flags = factory.availableTypes().stream()
                .map(TypeDescriptor::consumesEnergy).toList();

        assertEquals(List.of(true, false), flags);
    }

    @Test
    @DisplayName("отклоняет повторную регистрацию кода")
    void rejectsDuplicateCode() {
        assertThrows(DuplicateTypeCodeException.class,
                () -> factory.register("bicycle", "Ещё велосипед",
                        Bicycle.class,
                        (n, r) -> new Bicycle(n, r.name(), r.simplicity())));
    }

    @Test
    @DisplayName("неизвестный и пустой код дают UnknownTypeCodeException")
    void rejectsUnknownCode() {
        assertThrows(UnknownTypeCodeException.class,
                () -> factory.requireSupported("segway"));
        assertThrows(UnknownTypeCodeException.class,
                () -> factory.requireSupported(null));
    }

    @Test
    @DisplayName("список видов в порядке регистрации и неизменяем")
    void typesAreOrderedAndUnmodifiable() {
        assertEquals(List.of("scooter", "bicycle"), factory.availableTypes()
                .stream().map(TypeDescriptor::code).toList());
        assertThrows(UnsupportedOperationException.class,
                () -> factory.availableTypes().clear());
    }

    @Test
    @DisplayName("create с неизвестным видом в заявке — UnknownTypeCodeException")
    void createRejectsUnknownType() {
        assertThrows(UnknownTypeCodeException.class, () -> factory.create(1,
                TestData.intake().vehicleType("segway").build()));
    }

    @Test
    @DisplayName("пустая фабрика не знает ни одного вида")
    void emptyFactoryHasNoTypes() {
        assertEquals(List.of(), new VehicleFactory().availableTypes());
    }

    @Test
    @DisplayName("регистрация без кода — ошибка программиста")
    void registerRejectsNullCode() {
        assertThrows(NullPointerException.class,
                () -> factory.register(null, "Без кода", Bicycle.class,
                        (n, r) -> new Bicycle(n, r.name(), r.simplicity())));
    }
}

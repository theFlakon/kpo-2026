package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import domain.capability.EnergyConsumer;
import domain.exception.UnknownTypeCodeException;
import domain.inspection.InspectionResult;
import domain.inspection.IntakeRequest;
import domain.inspection.ServiceCenter;
import domain.item.Item;
import domain.vehicle.Vehicle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import support.FleetFixture;
import support.StubServiceCenter;
import support.TestData;

/**
 * Постановка на баланс — главный тест поведения.
 *
 * <p>Техосмотр подменяется тремя способами, каждый там, где он уместен:
 * fake {@link StubServiceCenter} (фиксированный ответ и запись вызовов),
 * лямбда (функциональный интерфейс) и mock из Mockito (проверка, что
 * осмотр не вызывался). Подмена через DI-контейнер — в
 * {@code config.ServiceCenterOverrideTest}.
 */
@DisplayName("FleetService")
class FleetServiceTest {

    @Nested
    @DisplayName("приём техники")
    class AcceptVehicle {

        @Test
        @DisplayName("пройденный техосмотр — транспорт в парке со следующим номером")
        void storesVehicleWhenInspectionPasses() {
            FleetFixture park = FleetFixture.with(StubServiceCenter.passing());

            AcceptanceResult result =
                    park.fleet().acceptVehicle(TestData.intake().build());

            assertTrue(result.isAccepted());
            assertEquals(1, result.getVehicle().orElseThrow()
                    .getInventoryNumber());
            assertEquals(1, park.reports().countVehicles());
        }

        @Test
        @DisplayName("отказ техосмотра — парк не меняется, номер не тратится")
        void rejectionChangesNothing() {
            // Решение техосмотра не зависит от данных заявки: заявка
            // идеальная, а отказ задан подменой. Так проверяется именно
            // реакция сервиса на решение, а не правила осмотра.
            FleetFixture rejecting =
                    FleetFixture.with(StubServiceCenter.failing("тест"));

            AcceptanceResult result =
                    rejecting.fleet().acceptVehicle(TestData.intake().build());

            assertFalse(result.isAccepted());
            assertEquals("тест", result.getInspection().reason());
            assertTrue(rejecting.repository().findAll().isEmpty());
            assertEquals(1, rejecting.fleet().registerItem(
                    TestData.item().build()).getInventoryNumber());
        }

        @Test
        @DisplayName("техосмотр получает ровно ту заявку, что пришла в сервис")
        void passesRequestToInspection() {
            StubServiceCenter inspector = StubServiceCenter.passing();
            IntakeRequest request = TestData.intake().build();

            FleetFixture.with(inspector).fleet().acceptVehicle(request);

            assertEquals(1, inspector.inspected().size());
            assertSame(request, inspector.inspected().get(0));
        }

        @Test
        @DisplayName("неизвестный вид отклоняется до техосмотра")
        void unknownTypeNeverReachesInspection() {
            ServiceCenter inspector = mock(ServiceCenter.class);
            FleetFixture park = FleetFixture.with(inspector);

            assertThrows(UnknownTypeCodeException.class,
                    () -> park.fleet().acceptVehicle(
                            TestData.intake().vehicleType("segway").build()));
            verifyNoInteractions(inspector);
        }

        @Test
        @DisplayName("техосмотр можно подменить лямбдой")
        void inspectionCanBeLambda() {
            FleetFixture park = FleetFixture.with(
                    request -> InspectionResult.fail("лямбда"));

            assertFalse(park.fleet().acceptVehicle(TestData.intake().build())
                    .isAccepted());
        }

        // Регистрация видов в AppConfig — лямбды, которые раскладывают поля
        // заявки по конструкторам. Ошибку в них (перепутанный вид, забытый
        // расход) компилятор не поймает, поэтому её ловит этот тест.
        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource({
            "scooter, ElectricScooter",
            "bicycle, Bicycle",
            "ebike,   ElectricBike"
        })
        @DisplayName("каждый вид создаётся своим классом с данными заявки")
        void mapsRequestToVehicle(String code, String expectedClass) {
            FleetFixture park = FleetFixture.with(StubServiceCenter.passing());

            Vehicle vehicle = park.fleet().acceptVehicle(TestData.intake()
                    .vehicleType(code).name("Модель").simplicity(7)
                    .dailyEnergyKwh(0.7).build()).getVehicle().orElseThrow();

            assertEquals(expectedClass, vehicle.getClass().getSimpleName());
            assertEquals("Модель", vehicle.getName());
            assertEquals(7, vehicle.getSimplicity());
            if (vehicle instanceof EnergyConsumer consumer) {
                assertEquals(0.7, consumer.getDailyEnergyKwh());
            }
        }

        @Test
        @DisplayName("null вместо заявки — ошибка программиста")
        void rejectsNullRequest() {
            FleetFixture park = FleetFixture.with(StubServiceCenter.passing());

            assertThrows(NullPointerException.class,
                    () -> park.fleet().acceptVehicle(null));
        }
    }

    @Nested
    @DisplayName("постановка вещей")
    class RegisterItem {

        @Test
        @DisplayName("вещи и транспорт делят одну нумерацию")
        void sharesNumberingWithVehicles() {
            FleetFixture park = FleetFixture.with(StubServiceCenter.passing());

            park.fleet().acceptVehicle(TestData.intake().build());

            assertEquals(2, park.fleet().registerItem(TestData.item().build())
                    .getInventoryNumber());
        }

        @Test
        @DisplayName("неизвестный вид вещи не тратит номер")
        void unknownTypeKeepsNumber() {
            FleetFixture park = FleetFixture.with(StubServiceCenter.passing());

            assertThrows(UnknownTypeCodeException.class,
                    () -> park.fleet().registerItem(
                            TestData.item().itemType("drone").build()));
            assertEquals(1, park.fleet().registerItem(TestData.item().build())
                    .getInventoryNumber());
        }

        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource({
            "helmet,  Helmet",
            "dock,    DockingStation",
            "cabinet, ChargingCabinet"
        })
        @DisplayName("каждый вид вещи создаётся своим классом с данными заявки")
        void mapsRequestToItem(String code, String expectedClass) {
            FleetFixture park = FleetFixture.with(StubServiceCenter.passing());

            Item item = park.fleet().registerItem(TestData.item().itemType(code)
                    .name("Вещь").dailyEnergyKwh(1.2).build());

            assertEquals(expectedClass, item.getClass().getSimpleName());
            assertEquals("Вещь", item.getName());
            if (item instanceof EnergyConsumer consumer) {
                assertEquals(1.2, consumer.getDailyEnergyKwh());
            }
        }

        @Test
        @DisplayName("вещь не проходит техосмотр")
        void itemsSkipInspection() {
            StubServiceCenter inspector = StubServiceCenter.failing("не нужен");
            FleetFixture park = FleetFixture.with(inspector);

            assertInstanceOf(Item.class,
                    park.fleet().registerItem(TestData.item().build()));
            assertTrue(inspector.inspected().isEmpty());
        }
    }

    @Test
    @DisplayName("списки видов для консоли берутся из фабрик")
    void exposesRegisteredTypes() {
        FleetService fleet = FleetFixture.with(StubServiceCenter.passing()).fleet();

        assertEquals(3, fleet.availableVehicleTypes().size());
        assertEquals(3, fleet.availableItemTypes().size());
    }
}

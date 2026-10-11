package cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.capability.EnergyConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import service.RuleBasedServiceCenter;
import support.FleetFixture;
import support.TestConsole;

/**
 * Сценарии меню целиком: ввод → сервисы → вывод.
 *
 * <p>Новый сценарий — это новый набор строк ввода и проверка вывода.
 * Номера пунктов и видов соответствуют меню и порядку регистрации
 * в {@code AppConfig}: 1 — электросамокат, 2 — велосипед; 1 — шлем,
 * 3 — зарядный шкаф.
 */
@DisplayName("ConsoleApp")
class ConsoleAppTest {

    /** Свежий парк с настоящим техосмотром. */
    private final FleetFixture park = FleetFixture.with(new RuleBasedServiceCenter(
            RuleBasedServiceCenter.DEFAULT_MIN_CONDITION));

    @Nested
    @DisplayName("пункт 1: приём техники")
    class AcceptVehicle {

        @Test
        @DisplayName("велосипед — расход не спрашивается")
        void acceptsBicycleWithoutEnergyQuestion() {
            // вид 2 (велосипед); название; простота; состояние; тормоза.
            String output = run("1", "2", "Stels", "7", "90", "да", "0");

            assertTrue(output.contains("Принято: №1"));
            assertFalse(output.contains("Суточный расход"));
            assertEquals(1, park.reports().countVehicles());
        }

        @Test
        @DisplayName("электросамокат — расход спрашивается и сохраняется")
        void acceptsScooterWithEnergy() {
            String output = run("1", "1", "Ninebot", "8", "0,4", "90", "да", "0");

            assertTrue(output.contains("Суточный расход"));
            EnergyConsumer scooter = park.repository()
                    .findAllOf(EnergyConsumer.class).get(0);
            assertEquals(0.4, scooter.getDailyEnergyKwh());
        }

        @Test
        @DisplayName("отказ техосмотра — причина на экране, парк пуст")
        void showsRejection() {
            String output = run("1", "2", "Старый", "7", "50", "нет", "0");

            assertTrue(output.contains("Отказ техосмотра: тормоза неисправны"));
            assertEquals(0, park.reports().countAll());
        }

        @Test
        @DisplayName("ошибка данных — сообщение и возврат в меню")
        void dataErrorReturnsToMenu() {
            String output = run("1", "2", "Stels", "11", "90", "да", "3", "0");

            assertTrue(output.contains("Ошибка: Простота"));
            assertTrue(output.contains("Всего на балансе: 0"));
        }
    }

    @Nested
    @DisplayName("пункт 2: вещи")
    class RegisterItem {

        @Test
        @DisplayName("шлем — расход не спрашивается")
        void registersHelmet() {
            String output = run("2", "1", "Шлем M", "0");

            assertTrue(output.contains("Поставлено на баланс: №1"));
            assertFalse(output.contains("Суточный расход"));
        }

        @Test
        @DisplayName("зарядный шкаф — расход спрашивается")
        void registersCabinetWithEnergy() {
            run("2", "3", "Шкаф", "2,5", "0");

            assertEquals(2.5, park.reports().totalDailyEnergyKwh());
        }
    }

    @Nested
    @DisplayName("пункты 3–7: отчёты и демонстрация")
    class Reports {

        @Test
        @DisplayName("каждый отчёт печатается своим пунктом")
        void eachReportHasMenuItem() {
            String output = run("3", "4", "5", "6", "0");

            assertTrue(output.contains("Всего на балансе"));
            assertTrue(output.contains("энергопотребление"));
            assertTrue(output.contains("Транспорт для новичков"));
            assertTrue(output.contains("Инвентарная ведомость"));
        }

        @Test
        @DisplayName("демонстрация запускается из меню и наполняет парк")
        void demoFromMenu() {
            run("7", "0");

            assertTrue(park.reports().countAll() > 0);
        }
    }

    @Test
    @DisplayName("неизвестный пункт меню не ломает приложение")
    void unknownMenuItem() {
        assertTrue(run("42", "0").contains("Нет такого пункта меню: '42'"));
    }

    @Test
    @DisplayName("пункт 0 завершает работу сразу")
    void exitStopsImmediately() {
        String output = run("0", "3");

        assertTrue(output.contains("До встречи"));
        assertFalse(output.contains("Всего на балансе"));
    }

    @Test
    @DisplayName("конец ввода посреди заявки завершает работу без ошибки")
    void endOfInputMidDialogExitsGracefully() {
        String output = run("1", "2", "Stels");

        assertTrue(output.contains("ввод завершён"));
        assertTrue(output.contains("До встречи"));
        assertEquals(0, park.reports().countAll());
    }

    private String run(String... lines) {
        TestConsole console = TestConsole.withInput(lines);
        DemoScenario demo = new DemoScenario(park.fleet(), park.reports(),
                console.printer());
        new ConsoleApp(park.fleet(), park.reports(), console.input(),
                console.printer(), demo).run();
        return console.output();
    }
}

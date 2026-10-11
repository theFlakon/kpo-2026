package support;

import config.AppConfig;
import domain.inspection.IntakeRequest;
import domain.item.ChargingCabinet;
import domain.item.Helmet;
import domain.item.ItemRequest;
import domain.vehicle.Bicycle;
import domain.vehicle.ElectricScooter;
import service.ItemFactory;
import service.VehicleFactory;

/**
 * Готовые тестовые данные (паттерн Object Mother).
 *
 * <p>Заявки возвращаются как заполненные корректными значениями
 * builder'ы: тест меняет только то поле, которое проверяет, например
 * {@code TestData.intake().simplicity(11).build()}. Так из теста видно,
 * что именно в нём важно.
 */
public final class TestData {

    // Коды должны совпадать с регистрацией в AppConfig, где они приватны.
    // Совпадение проверяет AppConfigTest, поэтому расхождение не пройдёт
    // незамеченным.
    /** Код вида «электросамокат». */
    public static final String SCOOTER = "scooter";

    /** Код вида «велосипед». */
    public static final String BICYCLE = "bicycle";

    /** Код вида «электровелосипед». */
    public static final String E_BIKE = "ebike";

    /** Код вида «шлем». */
    public static final String HELMET = "helmet";

    /** Код вида «док-станция». */
    public static final String DOCK = "dock";

    /** Код вида «зарядный шкаф». */
    public static final String CABINET = "cabinet";

    /** Простота, подходящая новичку при пороге по умолчанию. */
    public static final int EASY = 8;

    /** Простота, не подходящая новичку при пороге по умолчанию. */
    public static final int HARD = 3;

    /** Состояние, проходящее техосмотр по умолчанию, %. */
    public static final int GOOD_CONDITION = 95;

    private TestData() {
    }

    /**
     * Корректная заявка на исправный электросамокат.
     *
     * @return builder с заполненными полями
     */
    public static IntakeRequest.IntakeRequestBuilder intake() {
        return IntakeRequest.builder()
                .vehicleType(SCOOTER)
                .name("Ninebot Max G30")
                .simplicity(EASY)
                .dailyEnergyKwh(0.4)
                .technicalCondition(GOOD_CONDITION)
                .brakesOk(true);
    }

    /**
     * Корректная заявка на шлем.
     *
     * @return builder с заполненными полями
     */
    public static ItemRequest.ItemRequestBuilder item() {
        return ItemRequest.builder()
                .itemType(HELMET)
                .name("Шлем M");
    }

    /**
     * Фабрика транспорта с той же регистрацией, что в приложении.
     *
     * @return новая фабрика
     */
    public static VehicleFactory vehicleFactory() {
        // proxyBeanMethods = false делает методы AppConfig обычными:
        // их можно вызвать без контейнера и не дублировать регистрацию.
        return new AppConfig().vehicleFactory();
    }

    /**
     * Фабрика вещей с той же регистрацией, что в приложении.
     *
     * @return новая фабрика
     */
    public static ItemFactory itemFactory() {
        return new AppConfig().itemFactory();
    }

    /**
     * Велосипед с заданным номером и простотой.
     *
     * @param number     инвентарный номер
     * @param simplicity простота
     * @return велосипед
     */
    public static Bicycle bicycle(int number, int simplicity) {
        return new Bicycle(number, "Велосипед №" + number, simplicity);
    }

    /**
     * Электросамокат с заданным номером и расходом.
     *
     * @param number инвентарный номер
     * @param kwh    суточный расход
     * @return электросамокат
     */
    public static ElectricScooter scooter(int number, double kwh) {
        return new ElectricScooter(number, "Самокат №" + number, EASY, kwh);
    }

    /**
     * Шлем с заданным номером.
     *
     * @param number инвентарный номер
     * @return шлем
     */
    public static Helmet helmet(int number) {
        return new Helmet(number, "Шлем №" + number);
    }

    /**
     * Зарядный шкаф с заданным номером и расходом.
     *
     * @param number инвентарный номер
     * @param kwh    суточный расход
     * @return зарядный шкаф
     */
    public static ChargingCabinet cabinet(int number, double kwh) {
        return new ChargingCabinet(number, "Шкаф №" + number, kwh);
    }
}

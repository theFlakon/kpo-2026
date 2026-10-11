package cli;

import domain.exception.VyshKatException;
import domain.inspection.IntakeRequest;
import domain.item.ItemRequest;
import domain.vehicle.Vehicle;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import service.FleetService;
import service.ReportService;
import service.TypeDescriptor;

/**
 * Демонстрация всех сценариев приложения без участия пользователя.
 *
 * <p>Виды не зашиты в код: демонстрация принимает по одному объекту
 * каждого зарегистрированного вида. Новый вид транспорта или вещи
 * попадёт в демонстрацию без её правки.
 *
 * <p>Работает с тем же парком, что и меню: после демонстрации
 * в отчётах видна добавленная техника.
 */
@RequiredArgsConstructor
public final class DemoScenario {

    /** Простота, при которой транспорт подходит новичку. */
    private static final int EASY_SIMPLICITY = 8;

    /** Простота, при которой транспорт новичку не подходит. */
    private static final int HARD_SIMPLICITY = 4;

    /** Состояние, проходящее техосмотр, %. */
    private static final int GOOD_CONDITION = 90;

    /** Состояние ниже порога техосмотра, %. */
    private static final int POOR_CONDITION = 40;

    /** Расход первого электрического вида транспорта, кВт·ч. */
    private static final double BASE_VEHICLE_ENERGY_KWH = 0.4;

    /** Прибавка расхода для каждого следующего вида, кВт·ч. */
    private static final double VEHICLE_ENERGY_STEP_KWH = 0.1;

    /** Расход вещи-потребителя энергии, кВт·ч. */
    private static final double ITEM_ENERGY_KWH = 2.5;

    /** Отрицательный расход для демонстрации ошибки данных, кВт·ч. */
    private static final double INVALID_ENERGY_KWH = -1.0;

    /** Код вида, который заведомо не зарегистрирован. */
    private static final String UNKNOWN_TYPE_CODE = "segway";

    /** Сервис постановки на баланс. */
    @NonNull
    private final FleetService fleet;

    /** Сервис отчётов. */
    @NonNull
    private final ReportService reports;

    /** Форматирование вывода. */
    @NonNull
    private final ConsolePrinter printer;

    /**
     * Прогоняет все сценарии и печатает результат.
     */
    public void run() {
        printer.printSection("Демонстрация: приём техники каждого вида");
        List<TypeDescriptor> vehicleTypes = fleet.availableVehicleTypes();
        for (int i = 0; i < vehicleTypes.size(); i++) {
            acceptDemoVehicle(vehicleTypes.get(i), i);
        }

        // Отказы и ошибки показываются на первом зарегистрированном виде.
        TypeDescriptor sample = vehicleTypes.get(0);
        printer.printSection("Демонстрация: отказы техосмотра");
        attempt("неисправные тормоза", () -> printer.printAcceptance(
                fleet.acceptVehicle(request(sample, GOOD_CONDITION, false))));
        attempt("состояние ниже порога", () -> printer.printAcceptance(
                fleet.acceptVehicle(request(sample, POOR_CONDITION, true))));
        attempt("обе проблемы сразу", () -> printer.printAcceptance(
                fleet.acceptVehicle(request(sample, POOR_CONDITION, false))));

        printer.printSection("Демонстрация: ошибки данных");
        attempt("неизвестный вид '" + UNKNOWN_TYPE_CODE + "'",
                () -> fleet.acceptVehicle(IntakeRequest.builder()
                        .vehicleType(UNKNOWN_TYPE_CODE)
                        .name("Сегвей")
                        .simplicity(EASY_SIMPLICITY)
                        .technicalCondition(GOOD_CONDITION)
                        .brakesOk(true)
                        .build()));
        attempt("простота вне шкалы",
                () -> fleet.acceptVehicle(IntakeRequest.builder()
                        .vehicleType(sample.code())
                        .name(sample.displayName() + " (демо)")
                        .simplicity(Vehicle.MAX_SIMPLICITY + 1)
                        .technicalCondition(GOOD_CONDITION)
                        .brakesOk(true)
                        .build()));

        printer.printSection("Демонстрация: вещи на балансе");
        for (TypeDescriptor type : fleet.availableItemTypes()) {
            attempt("вещь «" + type.displayName() + "»",
                    () -> printer.printRegistered(fleet.registerItem(
                            itemRequest(type))));
        }
        attempt("вещь с отрицательным расходом",
                () -> fleet.registerItem(ItemRequest.builder()
                        .itemType(fleet.availableItemTypes().get(0).code())
                        .name("Неправильная вещь")
                        .dailyEnergyKwh(INVALID_ENERGY_KWH)
                        .build()));

        printer.printSection("Демонстрация: отчёты");
        printer.printAllReports(reports);
    }

    /**
     * Принимает исправный демонстрационный транспорт заданного вида.
     *
     * <p>Чётные по порядку виды получают высокую простоту, нечётные —
     * низкую, чтобы отчёт для новичков показал оба случая.
     *
     * @param type  вид транспорта
     * @param index порядковый номер вида в реестре, с нуля
     */
    private void acceptDemoVehicle(TypeDescriptor type, int index) {
        int simplicity = index % 2 == 0 ? EASY_SIMPLICITY : HARD_SIMPLICITY;
        double energy = type.consumesEnergy()
                ? BASE_VEHICLE_ENERGY_KWH + index * VEHICLE_ENERGY_STEP_KWH
                : 0;
        attempt("приём «" + type.displayName() + "»",
                () -> printer.printAcceptance(fleet.acceptVehicle(
                        IntakeRequest.builder()
                                .vehicleType(type.code())
                                .name(type.displayName() + " (демо)")
                                .simplicity(simplicity)
                                .dailyEnergyKwh(energy)
                                .technicalCondition(GOOD_CONDITION)
                                .brakesOk(true)
                                .build())));
    }

    /**
     * Собирает заявку для демонстрации отказа техосмотра.
     *
     * @param type      вид транспорта
     * @param condition техническое состояние, %
     * @param brakesOk  исправны ли тормоза
     * @return корректная заявка
     */
    private static IntakeRequest request(TypeDescriptor type, int condition,
                                         boolean brakesOk) {
        return IntakeRequest.builder()
                .vehicleType(type.code())
                .name(type.displayName() + " (на осмотр)")
                .simplicity(EASY_SIMPLICITY)
                .technicalCondition(condition)
                .brakesOk(brakesOk)
                .build();
    }

    /**
     * Собирает заявку на демонстрационную вещь.
     *
     * @param type вид вещи
     * @return корректная заявка
     */
    private static ItemRequest itemRequest(TypeDescriptor type) {
        return ItemRequest.builder()
                .itemType(type.code())
                .name(type.displayName() + " (демо)")
                .dailyEnergyKwh(type.consumesEnergy() ? ITEM_ENERGY_KWH : 0)
                .build();
    }

    /**
     * Выполняет шаг демонстрации и печатает ошибку данных, если она есть.
     *
     * <p>Создание заявки тоже происходит внутри шага, поэтому ошибки
     * в конструкторе заявки перехватываются здесь же.
     *
     * @param description что делает шаг
     * @param step        действие шага
     */
    private void attempt(String description, Runnable step) {
        printer.printStep(description);
        try {
            step.run();
        } catch (VyshKatException e) {
            printer.printError(e);
        }
    }
}
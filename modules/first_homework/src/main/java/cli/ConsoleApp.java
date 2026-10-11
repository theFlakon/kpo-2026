package cli;

import domain.exception.VyshKatException;
import domain.inspection.IntakeRequest;
import domain.item.ItemRequest;
import domain.vehicle.Vehicle;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import service.FleetService;
import service.ReportService;
import service.TypeDescriptor;

/**
 * Диалог с пользователем через текстовое меню.
 *
 * <p>Ошибки данных ({@link VyshKatException}) перехватываются на уровне
 * пункта меню: пользователь видит сообщение и возвращается в меню.
 * Любое другое исключение означает ошибку в коде и не скрывается.
 */
@RequiredArgsConstructor
public final class ConsoleApp {

    /** Сервис постановки на баланс. */
    @NonNull
    private final FleetService fleet;

    /** Сервис отчётов. */
    @NonNull
    private final ReportService reports;

    /** Чтение ввода. */
    @NonNull
    private final ConsoleInput input;

    /** Форматирование вывода. */
    @NonNull
    private final ConsolePrinter printer;

    /** Демонстрация всех сценариев. */
    @NonNull
    private final DemoScenario demo;

    /**
     * Запускает меню и работает до выбора «Выход» или конца ввода.
     */
    public void run() {
        printer.printWelcome();
        try {
            boolean running = true;
            while (running) {
                printer.printMenu();
                running = handle(input.readLine("Выберите пункт"));
            }
        } catch (InputClosedException e) {
            printer.printSection("ввод завершён");
        }
        printer.printGoodbye();
    }

    /**
     * Выполняет пункт меню.
     *
     * @param choice введённый номер пункта
     * @return {@code false}, если пользователь выбрал выход
     */
    private boolean handle(String choice) {
        // switch по пунктам — сознательно ограниченный OCP: новый пункт
        // требует правки этого метода. Паттерн «Команда» для восьми
        // пунктов был бы overengineering, а UI — самое дешёвое для
        // правки место. Новые виды техники меню при этом не меняют.
        try {
            switch (choice) {
                case "1" -> acceptVehicle();
                case "2" -> registerItem();
                case "3" -> printer.printCounts(reports);
                case "4" -> printer.printEnergy(reports);
                case "5" -> printer.printBeginners(reports);
                case "6" -> printer.printInventory(reports);
                case "7" -> demo.run();
                case "0" -> {
                    return false;
                }
                default -> printer.printUnknownCommand(choice);
            }
        } catch (VyshKatException e) {
            printer.printError(e);
        }
        return true;
    }

    /**
     * Принимает технику: спрашивает данные заявки и проводит техосмотр.
     */
    private void acceptVehicle() {
        TypeDescriptor type = input.chooseType("Вид транспорта",
                fleet.availableVehicleTypes());
        String name = input.readLine("Название модели");
        int simplicity = input.readInt("Простота для новичка ("
                + Vehicle.MIN_SIMPLICITY + "–" + Vehicle.MAX_SIMPLICITY + ")");
        // Расход спрашивается только у видов, потребляющих энергию:
        // признак вычислен фабрикой по классу вида.
        double energy = type.consumesEnergy()
                ? input.readDouble("Суточный расход, кВт·ч")
                : 0;
        int condition = input.readInt("Техническое состояние ("
                + IntakeRequest.MIN_CONDITION + "–"
                + IntakeRequest.MAX_CONDITION + "%)");
        boolean brakesOk = input.readYesNo("Тормоза исправны?");

        printer.printAcceptance(fleet.acceptVehicle(IntakeRequest.builder()
                .vehicleType(type.code())
                .name(name)
                .simplicity(simplicity)
                .dailyEnergyKwh(energy)
                .technicalCondition(condition)
                .brakesOk(brakesOk)
                .build()));
    }

    /**
     * Ставит вещь на баланс: спрашивает данные заявки.
     */
    private void registerItem() {
        TypeDescriptor type =
                input.chooseType("Вид вещи", fleet.availableItemTypes());
        String name = input.readLine("Название");
        double energy = type.consumesEnergy()
                ? input.readDouble("Суточный расход, кВт·ч")
                : 0;

        printer.printRegistered(fleet.registerItem(ItemRequest.builder()
                .itemType(type.code())
                .name(name)
                .dailyEnergyKwh(energy)
                .build()));
    }
}
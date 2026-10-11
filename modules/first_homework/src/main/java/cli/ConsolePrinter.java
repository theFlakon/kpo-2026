package cli;

import domain.capability.Inventoriable;
import domain.exception.VyshKatException;
import domain.item.Item;
import domain.vehicle.Vehicle;
import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import service.AcceptanceResult;
import service.ReportService;

/**
 * Форматирование вывода консоли.
 *
 * <p>Используется и меню, и демонстрацией, поэтому отчёты печатаются
 * одинаково в обоих сценариях. Сервисы возвращают данные, а строки
 * появляются только здесь.
 */
@RequiredArgsConstructor
public final class ConsolePrinter {

    // Явная локаль: дробные числа всегда с запятой, независимо
    // от настроек системы, на которой запущено приложение.
    /** Локаль для форматирования чисел. */
    private static final Locale LOCALE = Locale.forLanguageTag("ru-RU");

    /** Текст главного меню. */
    private static final String MENU = """

            Меню:
              1. Принять технику (через техосмотр)
              2. Поставить вещь на баланс
              3. Отчёт: число единиц
              4. Отчёт: суточное энергопотребление
              5. Транспорт для новичков
              6. Инвентарная ведомость
              7. Демонстрация всех сценариев
              0. Выход""";

    /** Поток вывода. */
    @NonNull
    private final PrintStream out;

    /**
     * Печатает приветствие.
     */
    public void printWelcome() {
        out.println("=== ВышКат — учёт парка ===");
        out.println("«Доедем до дедлайна»");
    }

    /**
     * Печатает главное меню.
     */
    public void printMenu() {
        out.println(MENU);
    }

    /**
     * Печатает заголовок раздела.
     *
     * @param title текст заголовка
     */
    public void printSection(String title) {
        out.println();
        out.println("--- " + title + " ---");
    }

    /**
     * Печатает описание шага демонстрации.
     *
     * @param description что делает шаг
     */
    public void printStep(String description) {
        out.println("> " + description);
    }

    /**
     * Печатает итог приёма техники.
     *
     * @param result итог приёма
     */
    public void printAcceptance(@NonNull AcceptanceResult result) {
        String reason = result.getInspection().reason();
        result.getVehicle().ifPresentOrElse(
                vehicle -> out.println("  Принято: " + describe(vehicle)
                        + " (" + reason + ")"),
                () -> out.println("  Отказ техосмотра: " + reason));
    }

    /**
     * Печатает вещь, поставленную на баланс.
     *
     * @param item поставленная вещь
     */
    public void printRegistered(@NonNull Item item) {
        out.println("  Поставлено на баланс: " + describe(item));
    }

    /**
     * Печатает сообщение об ошибке данных.
     *
     * @param error перехваченное исключение проекта
     */
    public void printError(@NonNull VyshKatException error) {
        out.println("  Ошибка: " + error.getMessage());
    }

    /**
     * Печатает сообщение о неизвестной команде меню.
     *
     * @param command введённая команда
     */
    public void printUnknownCommand(String command) {
        out.println("Нет такого пункта меню: '" + command + "'.");
    }

    /**
     * Печатает число единиц на балансе.
     *
     * @param reports источник отчётов
     */
    public void printCounts(@NonNull ReportService reports) {
        out.println("Всего на балансе: " + reports.countAll()
                + " (транспорт: " + reports.countVehicles()
                + ", вещи: " + reports.countItems() + ")");
    }

    /**
     * Печатает суммарное суточное энергопотребление.
     *
     * @param reports источник отчётов
     */
    public void printEnergy(@NonNull ReportService reports) {
        out.printf(LOCALE,
                "Суточное энергопотребление парка: %.2f кВт·ч%n",
                reports.totalDailyEnergyKwh());
    }

    /**
     * Печатает транспорт, подходящий новичкам.
     *
     * @param reports источник отчётов
     */
    public void printBeginners(@NonNull ReportService reports) {
        out.println("Транспорт для новичков ("
                + reports.beginnerRuleDescription() + "):");
        printList(reports.beginnerFriendlyVehicles(),
                "подходящего транспорта нет");
    }

    /**
     * Печатает названия и инвентарные номера всего, что на балансе.
     *
     * @param reports источник отчётов
     */
    public void printInventory(@NonNull ReportService reports) {
        out.println("Инвентарная ведомость:");
        printList(reports.inventory(), "на балансе ничего нет");
    }

    /**
     * Печатает все отчёты подряд.
     *
     * @param reports источник отчётов
     */
    public void printAllReports(@NonNull ReportService reports) {
        printCounts(reports);
        printEnergy(reports);
        printBeginners(reports);
        printInventory(reports);
    }

    /**
     * Печатает прощание.
     */
    public void printGoodbye() {
        out.println("До встречи! Доедем до дедлайна.");
    }

    /**
     * Печатает список объектов или сообщение, если он пуст.
     *
     * @param entries      объекты для вывода
     * @param emptyMessage сообщение для пустого списка
     */
    private void printList(List<? extends Inventoriable> entries,
                           String emptyMessage) {
        if (entries.isEmpty()) {
            out.println("  " + emptyMessage);
            return;
        }
        entries.forEach(entry -> out.println("  " + describe(entry)));
    }

    /**
     * Описывает объект одной строкой: номер, название, ветка учёта.
     *
     * @param entry объект на балансе
     * @return строка вида «№3  Шлем M  [вещь]»
     */
    private static String describe(Inventoriable entry) {
        // Ветка определяется по типу только для подписи в выводе:
        // логика приложения от этого не зависит.
        String category = entry instanceof Vehicle ? "транспорт" : "вещь";
        return String.format(LOCALE, "№%-4d %-28s [%s]",
                entry.getInventoryNumber(), entry.getName(), category);
    }
}

package adapter.in.console;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Чтение и разбор пользовательского ввода.
 *
 * <p>Проверяет только формат: при нечисловом вводе переспрашивает.
 * Диапазоны (балл от 0 до максимума и т. п.) не проверяет — это делает
 * модель.
 *
 * <p>Потоки ввода и вывода приходят через конструктор, поэтому в тестах
 * ввод подаётся строкой.
 */
@RequiredArgsConstructor
public final class ConsoleInput {

    /** Источник строк ввода. */
    @NonNull
    private final Scanner scanner;

    /** Поток для подсказок и сообщений о неверном формате. */
    @NonNull
    private final PrintStream out;

    /**
     * Читает строку после подсказки.
     *
     * @param prompt подсказка без двоеточия
     * @return строка без пробелов по краям
     * @throws InputClosedException если ввод закончился
     */
    public String readLine(String prompt) {
        out.print(prompt + ": ");
        if (!scanner.hasNextLine()) {
            throw new InputClosedException();
        }
        return scanner.nextLine().strip();
    }

    /**
     * Читает строку; пустой ввод заменяется значением по умолчанию.
     *
     * @param prompt       подсказка без двоеточия
     * @param defaultValue значение для пустого ввода
     * @return введённая строка или значение по умолчанию
     * @throws InputClosedException если ввод закончился
     */
    public String readLine(String prompt, @NonNull String defaultValue) {
        String line = readLine(prompt + " [" + defaultValue + "]");
        return line.isEmpty() ? defaultValue : line;
    }

    /**
     * Читает целое число, переспрашивая при неверном формате.
     *
     * @param prompt подсказка без двоеточия
     * @return введённое число
     * @throws InputClosedException если ввод закончился
     */
    public int readInt(String prompt) {
        while (true) {
            String line = readLine(prompt);
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                out.println("Нужно целое число, например 7.");
            }
        }
    }

    /**
     * Читает десятичное число, переспрашивая при неверном формате.
     *
     * <p>Принимает и точку, и запятую как разделитель дробной части.
     *
     * @param prompt подсказка без двоеточия
     * @return введённое число
     * @throws InputClosedException если ввод закончился
     */
    public BigDecimal readDecimal(String prompt) {
        while (true) {
            // Привычная запятая: «0,3» читается так же, как «0.3».
            String line = readLine(prompt).replace(',', '.');
            try {
                return new BigDecimal(line);
            } catch (NumberFormatException e) {
                out.println("Нужно число, например 0,3.");
            }
        }
    }

    /**
     * Предлагает выбрать элемент списка по номеру.
     *
     * @param title       заголовок списка
     * @param options     варианты
     * @param label       подпись варианта
     * @param emptyHint   подсказка, если вариантов нет
     * @param <T>         тип варианта
     * @return выбранный вариант
     * @throws NothingToChooseException если список пуст
     * @throws InputClosedException     если ввод закончился
     */
    public <T> T choose(String title, @NonNull List<T> options,
                        @NonNull Function<T, String> label,
                        String emptyHint) {
        if (options.isEmpty()) {
            throw new NothingToChooseException(emptyHint);
        }
        out.println(title + ":");
        for (int i = 0; i < options.size(); i++) {
            out.println("  " + (i + 1) + ". " + label.apply(options.get(i)));
        }
        while (true) {
            int choice = readInt("Номер");
            // Пользователь видит нумерацию с единицы, список — с нуля.
            if (choice >= 1 && choice <= options.size()) {
                return options.get(choice - 1);
            }
            out.println("Выберите номер от 1 до " + options.size() + ".");
        }
    }
}

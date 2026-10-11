package cli;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import service.TypeDescriptor;

/**
 * Чтение и разбор пользовательского ввода.
 *
 * <p>Проверяет только формат: при нечисловом вводе переспрашивает.
 * Диапазоны (простота 1–10 и т. п.) не проверяет — это делает домен.
 *
 * <p>Поток ввода и вывода приходит через конструктор, поэтому
 * в тестах ввод подаётся строкой.
 */
@RequiredArgsConstructor
public final class ConsoleInput {

    /** Варианты ответа «да». */
    private static final Set<String> YES = Set.of("да", "д", "yes", "y");

    /** Варианты ответа «нет». */
    private static final Set<String> NO = Set.of("нет", "н", "no", "n");

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
        return scanner.nextLine().trim();
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
     * Читает дробное число, переспрашивая при неверном формате.
     *
     * <p>Принимает и точку, и запятую как разделитель дробной части.
     *
     * @param prompt подсказка без двоеточия
     * @return введённое число
     * @throws InputClosedException если ввод закончился
     */
    public double readDouble(String prompt) {
        while (true) {
            // Привычная запятая: «0,4» читается так же, как «0.4».
            String line = readLine(prompt).replace(',', '.');
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                out.println("Нужно число, например 0,4.");
            }
        }
    }

    /**
     * Читает ответ «да» или «нет», переспрашивая при другом ответе.
     *
     * @param prompt подсказка без двоеточия
     * @return {@code true} для «да»
     * @throws InputClosedException если ввод закончился
     */
    public boolean readYesNo(String prompt) {
        while (true) {
            String answer = readLine(prompt + " (да/нет)")
                    .toLowerCase(Locale.ROOT);
            if (YES.contains(answer)) {
                return true;
            }
            if (NO.contains(answer)) {
                return false;
            }
            out.println("Ответьте «да» или «нет».");
        }
    }

    /**
     * Предлагает выбрать вид из списка по номеру.
     *
     * @param title   заголовок списка
     * @param options непустой список видов; виды регистрируются
     *                в {@code AppConfig}, поэтому список не пуст
     * @return выбранный вид
     * @throws InputClosedException если ввод закончился
     */
    public TypeDescriptor chooseType(String title,
                                     @NonNull List<TypeDescriptor> options) {
        out.println(title + ":");
        for (int i = 0; i < options.size(); i++) {
            out.println("  " + (i + 1) + ". "
                    + options.get(i).displayName());
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

package support;

import cli.ConsoleInput;
import cli.ConsolePrinter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Консоль для тестов: ввод из заранее заданных строк, вывод в буфер.
 *
 * <p>Когда строки ввода кончаются, консоль ведёт себя как при Ctrl+D,
 * поэтому сценарий можно не заканчивать командой выхода.
 */
public final class TestConsole {

    /** Буфер, куда пишется весь вывод. */
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    /** Поток вывода поверх буфера. */
    private final PrintStream out =
            new PrintStream(buffer, true, StandardCharsets.UTF_8);

    /** Чтение из заданных строк. */
    private final ConsoleInput input;

    /** Вывод в буфер. */
    private final ConsolePrinter printer = new ConsolePrinter(out);

    private TestConsole(String text) {
        Scanner scanner = new Scanner(new ByteArrayInputStream(
                text.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        this.input = new ConsoleInput(scanner, out);
    }

    /**
     * Создаёт консоль, которая по очереди «вводит» заданные строки.
     *
     * @param lines строки ввода
     * @return тестовая консоль
     */
    public static TestConsole withInput(String... lines) {
        return new TestConsole(lines.length == 0
                ? "" : String.join("\n", lines) + "\n");
    }

    /**
     * Чтение ввода.
     *
     * @return объект чтения
     */
    public ConsoleInput input() {
        return input;
    }

    /**
     * Форматирование вывода.
     *
     * @return объект вывода
     */
    public ConsolePrinter printer() {
        return printer;
    }

    /**
     * Всё, что было выведено.
     *
     * @return вывод как строка
     */
    public String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }
}

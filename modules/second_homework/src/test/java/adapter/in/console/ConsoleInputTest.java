package adapter.in.console;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.TestConsole;

@DisplayName("ConsoleInput — разбор ввода")
class ConsoleInputTest {

    @Test
    @DisplayName("строка обрезается; пустой ввод заменяется значением по умолчанию")
    void readsLines() {
        TestConsole console = TestConsole.withInput("  Анна  ", "", "свой.csv");

        assertEquals("Анна", console.input().readLine("ФИО"));
        assertEquals("f.csv", console.input().readLine("Файл", "f.csv"));
        assertEquals("свой.csv", console.input().readLine("Файл", "f.csv"));
    }

    @Test
    @DisplayName("целое: при неверном формате переспрашивает")
    void readsIntRetrying() {
        TestConsole console = TestConsole.withInput("семь", "7.5", "7");

        assertEquals(7, console.input().readInt("Балл"));
        assertTrue(console.output().contains("Нужно целое число"));
    }

    @Test
    @DisplayName("дробное: принимает запятую и точку, переспрашивает при ошибке")
    void readsDecimal() {
        TestConsole console = TestConsole.withInput("0,3", "abc", "30");

        assertEquals(new BigDecimal("0.3"), console.input().readDecimal("Вес"));
        assertEquals(new BigDecimal("30"), console.input().readDecimal("Вес"));
        assertTrue(console.output().contains("Нужно число"));
    }

    @Test
    @DisplayName("выбор по номеру с единицы; вне списка — переспрашивает")
    void choosesOption() {
        TestConsole console = TestConsole.withInput("0", "3", "2");

        String chosen = console.input().choose("Цвет", List.of("красный", "синий"),
                Function.identity(), "пусто");

        assertEquals("синий", chosen);
        assertTrue(console.output().contains("2. синий"));
        assertTrue(console.output().contains("Выберите номер от 1 до 2"));
    }

    @Test
    @DisplayName("пустой список — подсказка вместо вопроса")
    void nothingToChoose() {
        TestConsole console = TestConsole.withInput("1");

        NothingToChooseException error = assertThrows(NothingToChooseException.class,
                () -> console.input().choose("Студент", List.of(),
                        Object::toString, "Сначала добавьте студента"));

        assertEquals("Сначала добавьте студента", error.getMessage());
    }

    @Test
    @DisplayName("конец ввода — сигнал завершить работу")
    void endOfInput() {
        assertThrows(InputClosedException.class,
                () -> TestConsole.withInput().input().readLine("Пункт"));
    }
}

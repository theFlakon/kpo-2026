package cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import service.TypeDescriptor;
import support.TestConsole;

/**
 * Разбор ввода: формат проверяется, при ошибке консоль переспрашивает.
 */
@DisplayName("ConsoleInput")
class ConsoleInputTest {

    @Test
    @DisplayName("при нечисловом вводе переспрашивает")
    void reasksOnInvalidInt() {
        TestConsole console = TestConsole.withInput("abc", "7");

        assertEquals(7, console.input().readInt("Число"));
        assertTrue(console.output().contains("Нужно целое число"));
    }

    @Test
    @DisplayName("принимает запятую как разделитель дробной части")
    void acceptsCommaInDouble() {
        assertEquals(0.35,
                TestConsole.withInput("0,35").input().readDouble("Расход"));
    }

    @ParameterizedTest(name = "«{0}» → {1}")
    @CsvSource({"да, true", "Н, false", "yes, true", "no, false"})
    @DisplayName("понимает разные формы ответа да/нет")
    void parsesYesNo(String answer, boolean expected) {
        assertEquals(expected,
                TestConsole.withInput(answer).input().readYesNo("Вопрос"));
    }

    @Test
    @DisplayName("выбор вида: номер вне списка — переспросить")
    void choosesTypeWithinRange() {
        List<TypeDescriptor> types = List.of(
                new TypeDescriptor("a", "Первый", false),
                new TypeDescriptor("b", "Второй", true));

        TypeDescriptor chosen = TestConsole.withInput("3", "2").input()
                .chooseType("Вид", types);

        assertEquals("b", chosen.code());
    }

    @Test
    @DisplayName("конец ввода — InputClosedException")
    void signalsEndOfInput() {
        assertThrows(InputClosedException.class,
                () -> TestConsole.withInput().input().readLine("Что-нибудь"));
    }

    @Test
    @DisplayName("дробное число: при неверном формате переспрашивает")
    void reasksOnInvalidDouble() {
        TestConsole console = TestConsole.withInput("много", "1.5");

        assertEquals(1.5, console.input().readDouble("Расход"));
        assertTrue(console.output().contains("Нужно число"));
    }

    @Test
    @DisplayName("да/нет: на непонятный ответ переспрашивает")
    void reasksOnUnclearAnswer() {
        TestConsole console = TestConsole.withInput("может быть", "нет");

        assertFalse(console.input().readYesNo("Вопрос"));
        assertTrue(console.output().contains("Ответьте «да» или «нет»"));
    }

    @Test
    @DisplayName("строка возвращается без пробелов по краям")
    void trimsLine() {
        assertEquals("Stels",
                TestConsole.withInput("   Stels  ").input().readLine("Название"));
    }

    @Test
    @DisplayName("подсказка печатается перед вводом")
    void printsPrompt() {
        TestConsole console = TestConsole.withInput("x");

        console.input().readLine("Название модели");

        assertTrue(console.output().startsWith("Название модели: "));
    }
}

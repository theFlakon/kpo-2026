package domain.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import domain.exception.InvalidTextException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Texts")
class TextsTest {

    @Test
    @DisplayName("обязательный текст обрезается по краям")
    void trimsRequiredText() {
        assertEquals("Анна", Texts.requireText("  Анна \t", "ФИО", 10));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "\t\n"})
    @DisplayName("обязательный текст не может быть пустым; в сообщении — поле")
    void rejectsBlankRequiredText(String value) {
        InvalidTextException error = assertThrows(InvalidTextException.class,
                () -> Texts.requireText(value, "ФИО", 10));

        assertEquals("ФИО", error.getField());
        assertEquals("ФИО не может быть пустым", error.getMessage());
    }

    @Test
    @DisplayName("длина считается после обрезки: граница включительна")
    void lengthLimitIsInclusive() {
        assertEquals("abc", Texts.requireText(" abc ", "Поле", 3));
        assertThrows(InvalidTextException.class,
                () -> Texts.requireText("abcd", "Поле", 3));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  "})
    @DisplayName("необязательный текст: пустой превращается в пустую строку")
    void optionalTextMayBeEmpty(String value) {
        assertEquals("", Texts.optionalText(value, "Комментарий", 5));
    }

    @Test
    @DisplayName("необязательный текст тоже ограничен по длине")
    void optionalTextIsLimited() {
        assertEquals("ок", Texts.optionalText(" ок ", "Комментарий", 5));
        assertThrows(InvalidTextException.class,
                () -> Texts.optionalText("слишком", "Комментарий", 5));
    }
}

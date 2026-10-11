package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Описание вида не допускает пустых ссылок.
 */
@DisplayName("TypeDescriptor")
class TypeDescriptorTest {

    @Test
    @DisplayName("null вместо кода или названия — ошибка программиста (NPE)")
    void rejectsNulls() {
        assertThrows(NullPointerException.class,
                () -> new TypeDescriptor(null, "Самокат", true));
        assertThrows(NullPointerException.class,
                () -> new TypeDescriptor("scooter", null, true));
    }

    @Test
    @DisplayName("записи с одинаковыми полями равны")
    void valueEquality() {
        assertEquals(new TypeDescriptor("a", "А", true),
                new TypeDescriptor("a", "А", true));
    }
}

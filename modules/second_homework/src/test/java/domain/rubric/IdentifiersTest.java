package domain.rubric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import domain.exception.InvalidIdentifierException;
import domain.student.StudentId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Номера студентов и критериев")
class IdentifiersTest {

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    @DisplayName("номер должен быть положительным")
    void rejectsNonPositive(int value) {
        assertThrows(InvalidIdentifierException.class,
                () -> new CriterionId(value));
        InvalidIdentifierException error = assertThrows(
                InvalidIdentifierException.class, () -> new StudentId(value));
        assertEquals(value, error.getValue());
    }

    @Test
    @DisplayName("объекты-значения: равенство по номеру")
    void equalityByValue() {
        assertEquals(new CriterionId(3), new CriterionId(3));
        assertEquals(new StudentId(3), new StudentId(3));
    }
}

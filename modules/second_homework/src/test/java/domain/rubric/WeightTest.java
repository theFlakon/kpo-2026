package domain.rubric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import domain.exception.InvalidWeightException;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Weight — вклад критерия в итог")
class WeightTest {

    @ParameterizedTest
    @ValueSource(strings = {"0.0001", "0.3", "30", "100"})
    @DisplayName("положительный вес не больше 100 допустим")
    void acceptsPositiveWeight(String value) {
        assertEquals(0, new BigDecimal(value).compareTo(Weight.of(value).value()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-0.1", "100.01"})
    @DisplayName("нулевой, отрицательный и слишком большой вес отклоняется")
    void rejectsInvalidWeight(String value) {
        InvalidWeightException error = assertThrows(InvalidWeightException.class,
                () -> Weight.of(value));

        assertEquals(0, new BigDecimal(value).compareTo(error.getValue()));
    }

    @Test
    @DisplayName("0.30 и 0.3 — один и тот же вес")
    void normalizesTrailingZeros() {
        assertEquals(Weight.of("0.3"), Weight.of("0.30"));
        assertEquals(Weight.of("30"), new Weight(new BigDecimal("30.0")));
    }

    @Test
    @DisplayName("null вместо веса — ошибка программиста")
    void rejectsNull() {
        assertThrows(NullPointerException.class, () -> new Weight(null));
    }
}

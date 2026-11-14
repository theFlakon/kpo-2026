package domain.rubric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import domain.exception.InvalidMaxScoreException;
import domain.exception.InvalidScoreException;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Score — балл «N из M»")
class ScoreTest {

    @ParameterizedTest(name = "{0} из {1}")
    @CsvSource({"0, 10", "10, 10", "7, 10", "0, 1", "100, 100"})
    @DisplayName("балл в пределах [0, максимум] — границы включительно")
    void acceptsPointsWithinBounds(int points, int max) {
        Score score = new Score(points, max);

        assertEquals(points, score.points());
        assertEquals(max, score.maxPoints());
    }

    @ParameterizedTest(name = "{0} из {1}")
    @CsvSource({"-1, 10", "11, 10", "12, 10", "2, 1"})
    @DisplayName("балл вне пределов отклоняется: «12 из 10» не существует")
    void rejectsPointsOutOfBounds(int points, int max) {
        InvalidScoreException error = assertThrows(InvalidScoreException.class,
                () -> new Score(points, max));

        assertEquals(points, error.getPoints());
        assertEquals(max, error.getMaxPoints());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -5, 101})
    @DisplayName("максимум вне 1–100 отклоняется")
    void rejectsInvalidMax(int max) {
        assertThrows(InvalidMaxScoreException.class, () -> new Score(0, max));
    }

    @Test
    @DisplayName("доля от максимума: 7 из 10 — это 0,7")
    void ratio() {
        assertEquals(0, new BigDecimal("0.7").compareTo(new Score(7, 10).ratio()));
        assertEquals(0, BigDecimal.ONE.compareTo(new Score(20, 20).ratio()));
    }

    @Test
    @DisplayName("объект-значение: равенство по значению")
    void equalityByValue() {
        assertEquals(new Score(7, 10), new Score(7, 10));
    }
}

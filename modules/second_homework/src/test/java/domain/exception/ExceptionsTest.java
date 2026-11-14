package domain.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.ExportFailedException;
import application.UnknownPolicyException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Иерархия исключений")
class ExceptionsTest {

    static Stream<Arguments> allExceptions() {
        return Stream.of(
                Arguments.of(InvalidTextException.blank("Поле"), true),
                Arguments.of(InvalidTextException.tooLong("Поле", 5), true),
                Arguments.of(new InvalidIdentifierException("студента", 0), true),
                Arguments.of(new InvalidScoreException(12, 10), true),
                Arguments.of(new InvalidMaxScoreException(0, 1, 100), true),
                Arguments.of(new InvalidWeightException(BigDecimal.ZERO,
                        BigDecimal.TEN), true),
                Arguments.of(new UnknownCriterionException(9), false),
                Arguments.of(new UnknownStudentException(9), false),
                Arguments.of(new DuplicateCriterionException("Экзамен"), false),
                Arguments.of(new DuplicateEvaluationException(1), false),
                Arguments.of(new MissingEvaluationException(1), false),
                Arguments.of(new EmptyRubricException(), false),
                Arguments.of(new RemovalBlockedException("студента «А»"), false),
                Arguments.of(new UnknownPolicyException("x"), false),
                Arguments.of(new ExportFailedException("f.csv",
                        new IOException("нет доступа")), false));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allExceptions")
    @DisplayName("любое исключение проекта — PosException с понятным сообщением")
    void everyExceptionIsPosExceptionWithMessage(PosException error,
                                                 boolean invalidValue) {
        assertFalse(error.getMessage().isBlank());
        assertEquals(invalidValue, error instanceof InvalidValueException,
                "ветка иерархии: значение или правило");
    }

    @Test
    @DisplayName("ошибка выгрузки сохраняет исходную причину")
    void exportFailureKeepsCause() {
        IOException cause = new IOException("нет доступа");
        ExportFailedException error = new ExportFailedException("f.csv", cause);

        assertSame(cause, error.getCause());
        assertTrue(error.getMessage().contains("нет доступа"));
        assertInstanceOf(PosException.class, error);
    }
}

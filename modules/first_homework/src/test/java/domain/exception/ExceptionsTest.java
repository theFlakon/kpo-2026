package domain.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Иерархия исключений: всё перехватывается одним обработчиком
 * и объясняет пользователю, что пошло не так.
 *
 * <p>Новое исключение проекта добавляется одной строкой в
 * {@link #allExceptions()} — и сразу проверяется на общий контракт.
 */
@DisplayName("Исключения проекта")
class ExceptionsTest {

    static Stream<VyshKatException> allExceptions() {
        return Stream.of(
                new InvalidInventoryNumberException(0),
                new InvalidNameException(" "),
                new InvalidSimplicityException(11, 1, 10),
                new InvalidEnergyConsumptionException(-1),
                new InvalidTechnicalConditionException(101, 0, 100),
                new InvalidInspectionResultException(),
                new DuplicateInventoryNumberException(5),
                new UnknownTypeCodeException("segway"),
                new DuplicateTypeCodeException("scooter"));
    }

    @ParameterizedTest
    @MethodSource("allExceptions")
    @DisplayName("непроверяемое и с понятным сообщением")
    void isUncheckedWithMessage(VyshKatException error) {
        assertInstanceOf(RuntimeException.class, error);
        assertFalse(error.getMessage().isBlank());
    }

    @Test
    @DisplayName("ошибки данных объекта сгруппированы под InvalidAssetDataException")
    void assetDataErrorsAreGrouped() {
        assertInstanceOf(InvalidAssetDataException.class,
                new InvalidInventoryNumberException(0));
        assertInstanceOf(InvalidAssetDataException.class,
                new InvalidSimplicityException(0, 1, 10));
    }

    @Test
    @DisplayName("хранит отклонённое значение и показывает его в сообщении")
    void keepsRejectedValue() {
        InvalidSimplicityException error =
                new InvalidSimplicityException(11, 1, 10);

        assertEquals(11, error.getValue());
        assertEquals(1, error.getMin());
        assertEquals(10, error.getMax());
        assertTrue(error.getMessage().contains("11"));
    }

    @Test
    @DisplayName("неизвестный код null показывается в сообщении, а не роняет его")
    void unknownNullCodeHasReadableMessage() {
        UnknownTypeCodeException error = new UnknownTypeCodeException(null);

        assertNull(error.getCode());
        assertTrue(error.getMessage().contains("null"));
    }

    @Test
    @DisplayName("пустое название в сообщении видно в кавычках")
    void blankNameIsQuoted() {
        assertTrue(new InvalidNameException("  ").getMessage().contains("'  '"));
    }

    @Test
    @DisplayName("занятый номер доступен вызывающему коду")
    void duplicateNumberIsExposed() {
        assertEquals(5, new DuplicateInventoryNumberException(5)
                .getInventoryNumber());
    }

    @Test
    @DisplayName("состояние техосмотра — не ошибка данных объекта на балансе")
    void technicalConditionIsNotAssetData() {
        // instanceof не скомпилировался бы: классы заведомо не связаны.
        assertFalse(InvalidAssetDataException.class
                .isAssignableFrom(InvalidTechnicalConditionException.class));
    }
}

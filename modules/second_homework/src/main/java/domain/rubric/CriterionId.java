package domain.rubric;

import domain.exception.InvalidIdentifierException;

/**
 * Номер критерия в рубрикаторе.
 *
 * <p>Объект-значение: отдельный тип вместо {@code int}, чтобы номер
 * критерия нельзя было по ошибке передать вместо номера студента.
 *
 * @param value положительный номер
 */
public record CriterionId(int value) {

    /**
     * Проверяет номер.
     *
     * @throws InvalidIdentifierException если номер не положителен
     */
    public CriterionId {
        if (value <= 0) {
            throw new InvalidIdentifierException("критерия", value);
        }
    }
}

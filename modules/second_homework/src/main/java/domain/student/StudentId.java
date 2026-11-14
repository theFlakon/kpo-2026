package domain.student;

import domain.exception.InvalidIdentifierException;

/**
 * Номер студента на курсе.
 *
 * <p>Объект-значение: отдельный тип вместо {@code int}, чтобы номер
 * студента нельзя было по ошибке передать вместо номера критерия.
 *
 * @param value положительный номер
 */
public record StudentId(int value) {

    /**
     * Проверяет номер.
     *
     * @throws InvalidIdentifierException если номер не положителен
     */
    public StudentId {
        if (value <= 0) {
            throw new InvalidIdentifierException("студента", value);
        }
    }
}

package domain.rubric;

import domain.exception.InvalidMaxScoreException;
import domain.exception.InvalidScoreException;
import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Балл по критерию: «7 из 10».
 *
 * <p>Объект-значение: два балла «7 из 10» равны, у балла нет своей
 * идентичности. Максимум хранится вместе с баллом, поэтому балл
 * проверяет себя сам и некорректный балл («12 из 10») не может
 * существовать. Создаётся критерием: {@link Criterion#score(int)}.
 *
 * @param points    выставленный балл, от 0 до {@code maxPoints}
 * @param maxPoints максимум критерия
 */
public record Score(int points, int maxPoints) {

    /** Наименьший допустимый максимум критерия. */
    public static final int MIN_MAX_POINTS = 1;

    /** Наибольший допустимый максимум критерия. */
    public static final int MAX_MAX_POINTS = 100;

    /**
     * Проверяет балл и максимум.
     *
     * @throws InvalidMaxScoreException если максимум вне диапазона
     * @throws InvalidScoreException    если балл вне {@code [0, maxPoints]}
     */
    public Score {
        requireValidMax(maxPoints);
        if (points < 0 || points > maxPoints) {
            throw new InvalidScoreException(points, maxPoints);
        }
    }

    /**
     * Проверяет максимум критерия.
     *
     * <p>Публичный, потому что тот же диапазон проверяет критерий
     * при создании — правило записано в одном месте.
     *
     * @param maxPoints проверяемый максимум
     * @return тот же максимум, если он корректен
     * @throws InvalidMaxScoreException если максимум вне диапазона
     */
    public static int requireValidMax(int maxPoints) {
        if (maxPoints < MIN_MAX_POINTS || maxPoints > MAX_MAX_POINTS) {
            throw new InvalidMaxScoreException(maxPoints, MIN_MAX_POINTS,
                    MAX_MAX_POINTS);
        }
        return maxPoints;
    }

    /**
     * Доля от максимума: «7 из 10» — это 0.7.
     *
     * <p>Через долю сравнимы критерии с разными максимумами,
     * на ней построены правила итога.
     *
     * @return число от 0 до 1
     */
    public BigDecimal ratio() {
        return BigDecimal.valueOf(points).divide(
                BigDecimal.valueOf(maxPoints), MathContext.DECIMAL64);
    }
}

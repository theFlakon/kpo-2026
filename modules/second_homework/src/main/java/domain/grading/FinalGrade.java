package domain.grading;

import java.math.BigDecimal;
import lombok.NonNull;

/**
 * Итог студента по десятибалльной шкале.
 *
 * <p>Объект-значение: создаётся только расчётом
 * ({@link FinalGradeCalculator}) и не хранится, поэтому «отдельной цифры
 * наугад» быть не может. Вместе с числом хранится полнота: по скольким
 * критериям из скольких выставлены оценки — без неё итог текущей
 * успеваемости легко принять за окончательный.
 *
 * @param value          итог от 0 до {@link #MAX}
 * @param gradedCriteria число критериев с оценкой
 * @param totalCriteria  число критериев в рубрикаторе
 */
public record FinalGrade(@NonNull BigDecimal value, int gradedCriteria,
                         int totalCriteria) {

    /** Верх шкалы. */
    public static final BigDecimal MAX = BigDecimal.TEN;

    /** Наименьший положительный итог по шкале ВШЭ: 4 — «удовлетворительно». */
    public static final BigDecimal PASS_THRESHOLD = BigDecimal.valueOf(4);

    /**
     * Проверяет согласованность итога.
     *
     * <p>Нарушение здесь означает ошибку в правиле итога, а не в данных
     * пользователя, поэтому бросается стандартное исключение.
     *
     * @throws IllegalArgumentException если итог вне шкалы или число
     *                                  оценённых критериев некорректно
     */
    public FinalGrade {
        if (value.signum() < 0 || value.compareTo(MAX) > 0) {
            throw new IllegalArgumentException("Итог вне шкалы 0–10: "
                    + value.toPlainString());
        }
        if (gradedCriteria < 0 || gradedCriteria > totalCriteria) {
            throw new IllegalArgumentException("Оценено " + gradedCriteria
                    + " критериев из " + totalCriteria);
        }
    }

    /**
     * Сообщает, положителен ли итог.
     *
     * @return {@code true}, если итог не ниже {@link #PASS_THRESHOLD}
     */
    public boolean isPassing() {
        return value.compareTo(PASS_THRESHOLD) >= 0;
    }

    /**
     * Сообщает, выставлены ли оценки по всем критериям.
     *
     * @return {@code true}, если ведомость студента заполнена
     */
    public boolean isComplete() {
        return gradedCriteria == totalCriteria;
    }
}

package application;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.NonNull;

/**
 * Итоговая ведомость курса с аналитикой.
 *
 * <p>Аналитика считается из строк ведомости, а не хранится отдельно,
 * поэтому не может с ними разойтись.
 *
 * @param policyCode        код правила итога
 * @param policyDescription описание правила итога для читателя
 * @param grades            итоги студентов в порядке добавления на курс
 */
public record GradeSheet(@NonNull String policyCode,
                         @NonNull String policyDescription,
                         @NonNull List<StudentGrade> grades) {

    /** Знаков после запятой у среднего итога. */
    private static final int AVERAGE_SCALE = 2;

    /**
     * Делает список итогов неизменяемым.
     */
    public GradeSheet {
        grades = List.copyOf(grades);
    }

    /**
     * Средний итог по курсу.
     *
     * @return среднее с точностью до сотых или пустой {@code Optional},
     *         если студентов нет
     */
    public Optional<BigDecimal> average() {
        if (grades.isEmpty()) {
            return Optional.empty();
        }
        BigDecimal sum = values().reduce(BigDecimal.ZERO, BigDecimal::add);
        return Optional.of(sum
                .divide(BigDecimal.valueOf(grades.size()),
                        MathContext.DECIMAL64)
                .setScale(AVERAGE_SCALE, RoundingMode.HALF_UP));
    }

    /**
     * Наибольший итог.
     *
     * @return итог или пустой {@code Optional}, если студентов нет
     */
    public Optional<BigDecimal> best() {
        return values().max(BigDecimal::compareTo);
    }

    /**
     * Наименьший итог.
     *
     * @return итог или пустой {@code Optional}, если студентов нет
     */
    public Optional<BigDecimal> worst() {
        return values().min(BigDecimal::compareTo);
    }

    /**
     * Число студентов с неудовлетворительным итогом.
     *
     * @return число итогов ниже порога {@link domain.grading.FinalGrade#PASS_THRESHOLD}
     */
    public long failingCount() {
        return grades.stream()
                .filter(line -> !line.grade().isPassing())
                .count();
    }

    /**
     * Число студентов, у которых выставлены не все оценки.
     *
     * @return число незаполненных ведомостей
     */
    public long incompleteCount() {
        return grades.stream()
                .filter(line -> !line.grade().isComplete())
                .count();
    }

    /**
     * Итоги студентов как числа.
     *
     * @return поток итогов
     */
    private Stream<BigDecimal> values() {
        return grades.stream().map(line -> line.grade().value());
    }
}

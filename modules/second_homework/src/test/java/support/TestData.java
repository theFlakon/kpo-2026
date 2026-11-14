package support;

import domain.rubric.CriterionId;
import domain.rubric.Rubric;
import domain.rubric.Weight;
import domain.student.EvaluationFactory;
import domain.student.Student;
import domain.student.StudentId;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Готовые тестовые данные (паттерн Object Mother).
 *
 * <p>Рубрикатор повторяет демонстрацию: домашние задания (10 баллов,
 * вес 0,3), контрольная (20, 0,3), экзамен (100, 0,4). Разные максимумы
 * нужны, чтобы тесты итога проверяли нормировку, а не совпадение чисел.
 */
public final class TestData {

    /** Дата, которую показывают фиксированные часы. */
    public static final LocalDate TODAY = LocalDate.of(2026, 11, 12);

    /** Фиксированные часы: дата оценки в тестах предсказуема. */
    public static final Clock CLOCK = Clock.fixed(
            TODAY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

    /** Номер критерия «Домашние задания» в {@link #rubric()}. */
    public static final CriterionId HOMEWORK = new CriterionId(1);

    /** Номер критерия «Контрольная работа» в {@link #rubric()}. */
    public static final CriterionId TEST = new CriterionId(2);

    /** Номер критерия «Экзамен» в {@link #rubric()}. */
    public static final CriterionId EXAM = new CriterionId(3);

    /** Максимум домашних заданий. */
    public static final int HOMEWORK_MAX = 10;

    /**
     * Закрытый конструктор: класс содержит только статические методы.
     */
    private TestData() {
    }

    /**
     * Рубрикатор из трёх критериев с разными максимумами.
     *
     * @return новый рубрикатор
     */
    public static Rubric rubric() {
        Rubric rubric = new Rubric();
        rubric.addCriterion("Домашние задания", HOMEWORK_MAX, Weight.of("0.3"));
        rubric.addCriterion("Контрольная работа", 20, Weight.of("0.3"));
        rubric.addCriterion("Экзамен", 100, Weight.of("0.4"));
        return rubric;
    }

    /**
     * Фабрика оценок с фиксированными часами.
     *
     * @return фабрика
     */
    public static EvaluationFactory factory() {
        return new EvaluationFactory(CLOCK);
    }

    /**
     * Студент без оценок.
     *
     * @param id номер
     * @return студент «Студент №id»
     */
    public static Student student(int id) {
        return new Student(new StudentId(id), "Студент №" + id);
    }

    /**
     * Студент с оценками по рубрикатору {@link #rubric()}.
     *
     * @param id     номер студента
     * @param scores баллы по критериям в порядке рубрикатора;
     *               {@code null} — оценки нет
     * @return студент с оценками
     */
    public static Student graded(int id, Integer... scores) {
        Rubric rubric = rubric();
        Student student = student(id);
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] != null) {
                student.record(factory().create(rubric,
                        new CriterionId(i + 1), scores[i], ""));
            }
        }
        return student;
    }
}

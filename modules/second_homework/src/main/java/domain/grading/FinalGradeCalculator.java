package domain.grading;

import domain.exception.EmptyRubricException;
import domain.rubric.Criterion;
import domain.rubric.Rubric;
import domain.student.Student;
import java.util.List;
import lombok.NonNull;

/**
 * Расчёт итога — доменный сервис.
 *
 * <p>Правило не принадлежит ни одному агрегату: веса и максимумы живут
 * в рубрикаторе, оценки — у студента. Сервис соединяет их в строки
 * ведомости и передаёт выбранному правилу ({@link GradingPolicy}).
 *
 * <p>Состояния нет: итог считается заново при каждом запросе и поэтому
 * всегда согласован с текущим набором оценок. Исправили или отменили
 * оценку — следующий расчёт это учтёт, хранить и обновлять нечего.
 */
public final class FinalGradeCalculator {

    /**
     * Строит ведомость студента: по строке на каждый критерий.
     *
     * @param rubric  рубрикатор курса
     * @param student студент
     * @return строки в порядке рубрикатора, с оценкой или без
     */
    public List<GradeLine> lines(@NonNull Rubric rubric,
                                 @NonNull Student student) {
        return rubric.criteria().stream()
                .map(criterion -> lineFor(criterion, student))
                .toList();
    }

    /**
     * Считает итог студента по правилу.
     *
     * @param rubric  рубрикатор курса
     * @param student студент
     * @param policy  правило итога
     * @return итог с полнотой ведомости
     * @throws EmptyRubricException если в рубрикаторе нет критериев
     */
    public FinalGrade calculate(@NonNull Rubric rubric,
                                @NonNull Student student,
                                @NonNull GradingPolicy policy) {
        if (rubric.isEmpty()) {
            throw new EmptyRubricException();
        }
        List<GradeLine> lines = lines(rubric, student);
        int graded = (int) lines.stream().filter(GradeLine::isGraded).count();
        return new FinalGrade(policy.grade(lines), graded, lines.size());
    }

    /**
     * Строит строку ведомости по одному критерию.
     *
     * @param criterion критерий
     * @param student   студент
     * @return строка с оценкой, если она выставлена
     */
    private static GradeLine lineFor(Criterion criterion, Student student) {
        return student.evaluation(criterion.getId())
                .map(evaluation -> GradeLine.graded(criterion, evaluation))
                .orElseGet(() -> GradeLine.ungraded(criterion));
    }
}

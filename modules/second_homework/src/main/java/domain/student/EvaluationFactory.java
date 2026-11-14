package domain.student;

import domain.rubric.Criterion;
import domain.rubric.CriterionId;
import domain.rubric.Rubric;
import java.time.Clock;
import java.time.LocalDate;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Единая точка создания оценки (порождающий паттерн «Фабрика»).
 *
 * <p>Оценка зависит от трёх вещей, которых нет у неё самой: рубрикатора
 * (критерий должен существовать), максимума критерия (балл в пределах)
 * и текущей даты. Фабрика собирает их в одном месте: оценку по
 * несуществующему критерию или «12 из 10» создать нельзя, потому что
 * других способов создать оценку нет — конструктор {@link Evaluation}
 * закрыт от других пакетов.
 *
 * <p>Дата берётся из {@link Clock}, а не из системных часов напрямую:
 * в тестах часы подменяются, и дата оценки предсказуема.
 */
@RequiredArgsConstructor
public final class EvaluationFactory {

    /** Источник текущей даты. */
    @NonNull
    private final Clock clock;

    /**
     * Создаёт оценку по критерию рубрикатора.
     *
     * <p>Используется и для новой оценки, и для исправления:
     * исправленная оценка проходит те же проверки.
     *
     * @param rubric      рубрикатор курса
     * @param criterionId номер критерия
     * @param points      выставленный балл
     * @param comment     комментарий, допускается {@code null} или пустой
     * @return оценка с сегодняшней датой
     * @throws domain.exception.UnknownCriterionException
     *         если критерия нет в рубрикаторе
     * @throws domain.exception.InvalidScoreException
     *         если балл вне пределов критерия
     * @throws domain.exception.InvalidTextException
     *         если комментарий слишком длинный
     */
    public Evaluation create(@NonNull Rubric rubric,
                             @NonNull CriterionId criterionId, int points,
                             String comment) {
        Criterion criterion = rubric.criterion(criterionId);
        return new Evaluation(criterionId, criterion.score(points),
                LocalDate.now(clock), comment);
    }
}

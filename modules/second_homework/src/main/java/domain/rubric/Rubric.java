package domain.rubric;

import domain.exception.DuplicateCriterionException;
import domain.exception.UnknownCriterionException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.ToString;

/**
 * Рубрикатор курса — корень агрегата критериев.
 *
 * <p>Охраняет инварианты, которые не может проверить отдельный критерий:
 * названия уникальны (без учёта регистра), номера не повторяются даже
 * после удаления, а оценку можно связать только с критерием отсюда
 * ({@link #criterion} бросает исключение для неизвестного номера).
 *
 * <p>На курсе один рубрикатор; появятся несколько курсов — рубрикатор
 * получит номер курса, а правила останутся прежними.
 */
@ToString
public final class Rubric {

    /** Номер первого критерия. */
    private static final int FIRST_ID = 1;

    /** Критерии по номеру в порядке добавления. */
    private final Map<CriterionId, Criterion> criteria =
            new LinkedHashMap<>();

    /** Номер, который получит следующий критерий. */
    private int nextId = FIRST_ID;

    /**
     * Добавляет критерий.
     *
     * <p>Номер расходуется только при успешном добавлении: ошибка
     * в данных не оставляет дыр в нумерации.
     *
     * @param name     непустое название, уникальное в рубрикаторе
     * @param maxScore максимальный балл
     * @param weight   вклад в итог
     * @return добавленный критерий
     * @throws domain.exception.InvalidValueException
     *         если название или максимум некорректны
     * @throws DuplicateCriterionException если название уже занято
     */
    public Criterion addCriterion(String name, int maxScore,
                                  @NonNull Weight weight) {
        Criterion candidate = new Criterion(new CriterionId(nextId), name,
                maxScore, weight);
        if (hasName(candidate.getName())) {
            throw new DuplicateCriterionException(candidate.getName());
        }
        criteria.put(candidate.getId(), candidate);
        nextId++;
        return candidate;
    }

    /**
     * Находит критерий по номеру.
     *
     * @param id номер критерия
     * @return критерий
     * @throws UnknownCriterionException если такого критерия нет
     */
    public Criterion criterion(@NonNull CriterionId id) {
        Criterion criterion = criteria.get(id);
        if (criterion == null) {
            throw new UnknownCriterionException(id.value());
        }
        return criterion;
    }

    /**
     * Удаляет критерий.
     *
     * <p>Есть ли по критерию оценки, рубрикатор не знает: оценки
     * хранятся у студентов. Эту проверку делает сценарий удаления.
     *
     * @param id номер критерия
     * @return удалённый критерий
     * @throws UnknownCriterionException если такого критерия нет
     */
    public Criterion removeCriterion(@NonNull CriterionId id) {
        Criterion removed = criterion(id);
        criteria.remove(id);
        return removed;
    }

    /**
     * Возвращает все критерии.
     *
     * @return неизменяемый список в порядке добавления
     */
    public List<Criterion> criteria() {
        return List.copyOf(criteria.values());
    }

    /**
     * Сообщает, пуст ли рубрикатор.
     *
     * @return {@code true}, если критериев нет
     */
    public boolean isEmpty() {
        return criteria.isEmpty();
    }

    /**
     * Проверяет, занято ли название.
     *
     * @param name название без пробелов по краям
     * @return {@code true}, если критерий с таким названием есть
     */
    private boolean hasName(String name) {
        // «Экзамен» и «экзамен» — один критерий для читателя ведомости.
        return criteria.values().stream()
                .anyMatch(existing -> existing.getName()
                        .equalsIgnoreCase(name));
    }
}

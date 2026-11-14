package domain.rubric;

import domain.shared.Texts;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Критерий оценивания: за что ставят балл.
 *
 * <p>Сущность внутри агрегата {@link Rubric}: идентичность — номер,
 * равенство — по номеру. Конструктор закрыт от других пакетов: критерий
 * появляется только через {@link Rubric#addCriterion}, иначе рубрикатор
 * не смог бы гарантировать уникальность названий.
 *
 * <p>Максимум и вес не меняются: от максимума зависят уже выставленные
 * оценки, и его правка задним числом сломала бы правило «балл не выходит
 * за пределы критерия».
 */
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Criterion {

    /** Предельная длина названия. */
    public static final int MAX_NAME_LENGTH = 60;

    /** Номер в рубрикаторе. */
    @EqualsAndHashCode.Include
    private final CriterionId id;

    /** Название, например «Домашние задания». */
    private final String name;

    /** Максимальный балл. */
    private final int maxScore;

    /** Вклад в итог. */
    private final Weight weight;

    /**
     * Создаёт критерий с проверкой инвариантов.
     *
     * @param id       номер в рубрикаторе
     * @param name     непустое название
     * @param maxScore максимальный балл, от {@value Score#MIN_MAX_POINTS}
     *                 до {@value Score#MAX_MAX_POINTS}
     * @param weight   вклад в итог
     * @throws domain.exception.InvalidValueException
     *         если название или максимум некорректны
     */
    Criterion(@NonNull CriterionId id, String name, int maxScore,
              @NonNull Weight weight) {
        this.id = id;
        this.name = Texts.requireText(name, "Название критерия",
                MAX_NAME_LENGTH);
        this.maxScore = Score.requireValidMax(maxScore);
        this.weight = weight;
    }

    /**
     * Создаёт балл по этому критерию.
     *
     * <p>Единственное место, где балл связывается с максимумом критерия:
     * «12 из 10» отклоняется здесь.
     *
     * @param points выставленный балл
     * @return балл с максимумом критерия
     * @throws domain.exception.InvalidScoreException
     *         если балл вне {@code [0, maxScore]}
     */
    public Score score(int points) {
        return new Score(points, maxScore);
    }
}

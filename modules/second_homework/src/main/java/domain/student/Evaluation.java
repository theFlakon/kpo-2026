package domain.student;

import domain.rubric.CriterionId;
import domain.rubric.Score;
import domain.shared.Texts;
import java.time.LocalDate;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Оценка: «этому студенту по этому критерию поставили такой балл».
 *
 * <p>Объект-значение внутри агрегата {@link Student}: у оценки нет
 * собственной жизни вне студента, а «исправить оценку» означает заменить
 * её новой. Поэтому класс неизменяем, а равенство — по всем полям.
 *
 * <p>Конструктор закрыт от других пакетов: оценку создаёт только
 * {@link EvaluationFactory}, которая проверила связь с рубрикатором.
 * Обычный класс, а не record, потому что конструктор записи нельзя
 * сделать менее доступным, чем саму запись.
 */
@Getter
@ToString
@EqualsAndHashCode
public final class Evaluation {

    /** Предельная длина комментария. */
    public static final int MAX_COMMENT_LENGTH = 200;

    /** Критерий из рубрикатора — по номеру, а не ссылкой на объект. */
    private final CriterionId criterionId;

    /** Балл с максимумом критерия. */
    private final Score score;

    /** Дата выставления. */
    private final LocalDate date;

    /** Комментарий преподавателя; пустая строка — без комментария. */
    @Getter(AccessLevel.NONE)
    private final String comment;

    /**
     * Создаёт оценку.
     *
     * @param criterionId критерий из рубрикатора
     * @param score       балл, уже проверенный по максимуму критерия
     * @param date        дата выставления
     * @param comment     комментарий, допускается {@code null} или пустой
     * @throws domain.exception.InvalidTextException
     *         если комментарий длиннее {@value #MAX_COMMENT_LENGTH}
     */
    Evaluation(@NonNull CriterionId criterionId, @NonNull Score score,
               @NonNull LocalDate date, String comment) {
        this.criterionId = criterionId;
        this.score = score;
        this.date = date;
        this.comment = Texts.optionalText(comment, "Комментарий",
                MAX_COMMENT_LENGTH);
    }

    /**
     * Возвращает комментарий преподавателя.
     *
     * @return комментарий или пустой {@code Optional}
     */
    public Optional<String> getComment() {
        return comment.isEmpty() ? Optional.empty() : Optional.of(comment);
    }
}

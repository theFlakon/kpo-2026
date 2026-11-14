package adapter.out.persistence;

import domain.rubric.Rubric;
import domain.rubric.RubricRepository;
import lombok.NonNull;

/**
 * Рубрикатор в памяти процесса.
 *
 * <p>На курсе один рубрикатор, поэтому хранится одна ссылка.
 */
public final class InMemoryRubricRepository implements RubricRepository {

    /** Рубрикатор курса; в начале работы пустой. */
    private Rubric rubric = new Rubric();

    /**
     * {@inheritDoc}
     */
    @Override
    public Rubric get() {
        return rubric;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(@NonNull Rubric updated) {
        rubric = updated;
    }
}

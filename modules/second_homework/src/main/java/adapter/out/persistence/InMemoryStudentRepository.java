package adapter.out.persistence;

import domain.student.Student;
import domain.student.StudentId;
import domain.student.StudentRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;

/**
 * Студенты в памяти процесса.
 *
 * <p>Номера выдаются по возрастанию и не используются повторно после
 * удаления: иначе в выгруженной ранее ведомости номер указывал бы
 * на другого человека.
 *
 * <p>Не потокобезопасно: консольное приложение однопоточное.
 */
public final class InMemoryStudentRepository implements StudentRepository {

    /** Номер первого студента. */
    private static final int FIRST_ID = 1;

    /** Студенты по номеру в порядке добавления. */
    private final Map<StudentId, Student> storage = new LinkedHashMap<>();

    /** Номер, который будет выдан следующим. */
    private int nextId = FIRST_ID;

    /**
     * {@inheritDoc}
     */
    @Override
    public StudentId nextId() {
        return new StudentId(nextId++);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(@NonNull Student student) {
        storage.put(student.getId(), student);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Student> find(@NonNull StudentId id) {
        return Optional.ofNullable(storage.get(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Student> findAll() {
        return List.copyOf(storage.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(@NonNull StudentId id) {
        storage.remove(id);
    }
}

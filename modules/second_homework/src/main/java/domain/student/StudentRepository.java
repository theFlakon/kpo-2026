package domain.student;

import domain.exception.UnknownStudentException;
import java.util.List;
import java.util.Optional;

/**
 * Хранилище студентов — порт предметной области.
 *
 * <p>Интерфейс лежит рядом с агрегатом, а реализация — во внешнем слое
 * адаптеров. Номер нового студента выдаёт хранилище: оно знает, какие
 * номера уже заняты.
 */
public interface StudentRepository {

    /**
     * Выдаёт номер для нового студента.
     *
     * @return номер, не занятый ни одним студентом
     */
    StudentId nextId();

    /**
     * Сохраняет нового или изменённого студента.
     *
     * @param student студент
     */
    void save(Student student);

    /**
     * Находит студента по номеру.
     *
     * @param id номер
     * @return студент или пустой {@code Optional}
     */
    Optional<Student> find(StudentId id);

    /**
     * Находит студента, который обязан существовать.
     *
     * <p>Метод по умолчанию: «не нашли — ошибка» одинаково для любого
     * хранилища и не повторяется в каждом сценарии.
     *
     * @param id номер
     * @return студент
     * @throws UnknownStudentException если студента нет
     */
    default Student get(StudentId id) {
        return find(id).orElseThrow(
                () -> new UnknownStudentException(id.value()));
    }

    /**
     * Возвращает всех студентов.
     *
     * @return неизменяемый список в порядке добавления
     */
    List<Student> findAll();

    /**
     * Удаляет студента.
     *
     * @param id номер
     */
    void remove(StudentId id);
}

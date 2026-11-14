package adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.exception.UnknownStudentException;
import domain.rubric.Rubric;
import domain.student.Student;
import domain.student.StudentId;
import domain.student.StudentRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Хранилища в памяти")
class InMemoryRepositoriesTest {

    @Nested
    @DisplayName("студенты")
    class Students {

        private final StudentRepository repository = new InMemoryStudentRepository();

        private Student add(String name) {
            Student student = new Student(repository.nextId(), name);
            repository.save(student);
            return student;
        }

        @Test
        @DisplayName("номера по возрастанию и не используются повторно")
        void idsAreNotReused() {
            Student anna = add("Анна");
            repository.remove(anna.getId());

            assertEquals(new StudentId(1), anna.getId());
            assertEquals(new StudentId(2), repository.nextId());
        }

        @Test
        @DisplayName("поиск, список в порядке добавления и удаление")
        void findListRemove() {
            Student anna = add("Анна");
            Student boris = add("Борис");

            assertEquals(Optional.of(boris), repository.find(boris.getId()));
            assertEquals(List.of(anna, boris), repository.findAll());

            repository.remove(anna.getId());

            assertEquals(List.of(boris), repository.findAll());
            assertTrue(repository.find(anna.getId()).isEmpty());
        }

        @Test
        @DisplayName("get: неизвестный студент — ошибка модели")
        void getUnknown() {
            UnknownStudentException error = assertThrows(UnknownStudentException.class,
                    () -> repository.get(new StudentId(7)));

            assertEquals(7, error.getStudentId());
        }

        @Test
        @DisplayName("список нельзя изменить снаружи")
        void findAllIsUnmodifiable() {
            add("Анна");

            assertThrows(UnsupportedOperationException.class,
                    () -> repository.findAll().clear());
        }
    }

    @Test
    @DisplayName("рубрикатор: сначала пустой, после сохранения — сохранённый")
    void rubricRepository() {
        InMemoryRubricRepository repository = new InMemoryRubricRepository();
        Rubric replacement = new Rubric();

        assertTrue(repository.get().isEmpty());
        assertSame(repository.get(), repository.get());

        repository.save(replacement);

        assertSame(replacement, repository.get());
    }
}

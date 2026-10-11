package repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.capability.EnergyConsumer;
import domain.capability.Inventoriable;
import domain.exception.DuplicateInventoryNumberException;
import domain.item.Item;
import domain.vehicle.Vehicle;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import support.TestData;

/**
 * Хранилище парка: уникальность номеров и выборки по ролям.
 */
@DisplayName("InMemoryInventoryRepository")
class InMemoryInventoryRepositoryTest {

    private InventoryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryInventoryRepository();
    }

    @Nested
    @DisplayName("add")
    class Add {

        // Транспорт и вещь с одним номером по equals не равны, поэтому
        // этот тест проверяет, что дубликат ищется именно по номеру.
        @Test
        @DisplayName("отклоняет занятый номер даже из другой ветки учёта")
        void rejectsDuplicateNumberAcrossBranches() {
            repository.add(TestData.bicycle(1, 5));

            assertThrows(DuplicateInventoryNumberException.class,
                    () -> repository.add(TestData.helmet(1)));
        }

        @Test
        @DisplayName("при дубликате сохраняет исходный объект")
        void duplicateKeepsOriginal() {
            Inventoriable original = TestData.bicycle(1, 5);
            repository.add(original);

            assertThrows(DuplicateInventoryNumberException.class,
                    () -> repository.add(TestData.helmet(1)));
            assertSame(original, repository.findAll().get(0));
        }

        @Test
        @DisplayName("null — ошибка программиста, а не данных")
        void rejectsNull() {
            assertThrows(NullPointerException.class, () -> repository.add(null));
        }
    }

    @Nested
    @DisplayName("выборки")
    class Queries {

        @BeforeEach
        void fillPark() {
            repository.add(TestData.bicycle(1, 5));
            repository.add(TestData.scooter(2, 0.4));
            repository.add(TestData.helmet(3));
            repository.add(TestData.cabinet(4, 2.5));
        }

        @Test
        @DisplayName("findAll возвращает всё в порядке добавления")
        void findAllKeepsInsertionOrder() {
            assertEquals(List.of(1, 2, 3, 4), repository.findAll().stream()
                    .map(Inventoriable::getInventoryNumber).toList());
        }

        @Test
        @DisplayName("findAllOf отбирает по роли независимо от ветки")
        void findAllOfSelectsByRole() {
            List<Integer> consumers = repository
                    .findAllOf(EnergyConsumer.class).stream()
                    .map(c -> ((Inventoriable) c).getInventoryNumber())
                    .toList();

            assertEquals(List.of(2, 4), consumers);
            assertEquals(2, repository.findAllOf(Vehicle.class).size());
        }

        @Test
        @DisplayName("результаты нельзя изменить снаружи")
        void resultsAreUnmodifiable() {
            assertThrows(UnsupportedOperationException.class,
                    () -> repository.findAll().clear());
            assertThrows(UnsupportedOperationException.class,
                    () -> repository.findAllOf(Vehicle.class).clear());
        }

        @Test
        @DisplayName("findAllOf по общей роли возвращает всё")
        void findAllOfInventoriableReturnsEverything() {
            assertEquals(4, repository.findAllOf(Inventoriable.class).size());
            assertEquals(2, repository.findAllOf(Item.class).size());
        }

        @Test
        @DisplayName("ранее выданный список — снимок: новые объекты в нём не появляются")
        void findAllReturnsSnapshot() {
            List<Inventoriable> before = repository.findAll();

            repository.add(TestData.helmet(5));

            assertEquals(4, before.size());
            assertEquals(5, repository.findAll().size());
        }

        @Test
        @DisplayName("тип для выборки обязателен")
        void findAllOfRejectsNullType() {
            assertThrows(NullPointerException.class,
                    () -> repository.findAllOf(null));
        }
    }

    @Test
    @DisplayName("пустое хранилище отдаёт пустые списки")
    void emptyRepositoryReturnsEmptyLists() {
        assertTrue(repository.findAll().isEmpty());
        assertTrue(repository.findAllOf(EnergyConsumer.class).isEmpty());
    }
}

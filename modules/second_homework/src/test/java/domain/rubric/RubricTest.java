package domain.rubric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.exception.DuplicateCriterionException;
import domain.exception.InvalidMaxScoreException;
import domain.exception.InvalidScoreException;
import domain.exception.InvalidTextException;
import domain.exception.UnknownCriterionException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Rubric — агрегат критериев")
class RubricTest {

    private static final Weight WEIGHT = Weight.of("1");

    private final Rubric rubric = new Rubric();

    @Nested
    @DisplayName("добавление критерия")
    class AddCriterion {

        @Test
        @DisplayName("критерии получают номера по порядку и хранят данные")
        void assignsSequentialIds() {
            Criterion first = rubric.addCriterion("ДЗ", 10, Weight.of("0.3"));
            Criterion second = rubric.addCriterion(" Экзамен ", 100, WEIGHT);

            assertEquals(new CriterionId(1), first.getId());
            assertEquals(new CriterionId(2), second.getId());
            assertEquals("Экзамен", second.getName());
            assertEquals(100, second.getMaxScore());
            assertEquals(Weight.of("0.3"), first.getWeight());
            assertEquals(List.of(first, second), rubric.criteria());
        }

        @ParameterizedTest
        @ValueSource(strings = {"Экзамен", "экзамен", "  ЭКЗАМЕН  "})
        @DisplayName("название уникально без учёта регистра и пробелов")
        void rejectsDuplicateName(String duplicate) {
            rubric.addCriterion("Экзамен", 100, WEIGHT);

            assertThrows(DuplicateCriterionException.class,
                    () -> rubric.addCriterion(duplicate, 10, WEIGHT));
            assertEquals(1, rubric.criteria().size());
        }

        @Test
        @DisplayName("ошибка в данных не тратит номер")
        void failedAdditionKeepsNumbering() {
            rubric.addCriterion("ДЗ", 10, WEIGHT);
            assertThrows(InvalidMaxScoreException.class,
                    () -> rubric.addCriterion("Проект", 0, WEIGHT));
            assertThrows(DuplicateCriterionException.class,
                    () -> rubric.addCriterion("ДЗ", 10, WEIGHT));

            assertEquals(new CriterionId(2),
                    rubric.addCriterion("Проект", 10, WEIGHT).getId());
        }

        @Test
        @DisplayName("пустое и слишком длинное название отклоняется")
        void rejectsInvalidName() {
            assertThrows(InvalidTextException.class,
                    () -> rubric.addCriterion(" ", 10, WEIGHT));
            assertThrows(InvalidTextException.class,
                    () -> rubric.addCriterion("я".repeat(Criterion.MAX_NAME_LENGTH + 1),
                            10, WEIGHT));
            assertTrue(rubric.isEmpty());
        }
    }

    @Nested
    @DisplayName("поиск и удаление")
    class FindAndRemove {

        @Test
        @DisplayName("неизвестный номер — ошибка с этим номером")
        void unknownCriterion() {
            UnknownCriterionException error = assertThrows(
                    UnknownCriterionException.class,
                    () -> rubric.criterion(new CriterionId(7)));

            assertEquals(7, error.getCriterionId());
        }

        @Test
        @DisplayName("номера не используются повторно после удаления")
        void idsAreNotReused() {
            rubric.addCriterion("A", 10, WEIGHT);
            Criterion removed = rubric.addCriterion("B", 10, WEIGHT);

            assertEquals(removed, rubric.removeCriterion(removed.getId()));
            assertEquals(new CriterionId(3),
                    rubric.addCriterion("C", 10, WEIGHT).getId());
            assertThrows(UnknownCriterionException.class,
                    () -> rubric.removeCriterion(removed.getId()));
        }

        @Test
        @DisplayName("список критериев нельзя изменить снаружи")
        void criteriaAreUnmodifiable() {
            rubric.addCriterion("A", 10, WEIGHT);

            assertThrows(UnsupportedOperationException.class,
                    () -> rubric.criteria().clear());
            assertFalse(rubric.isEmpty());
        }
    }

    @Nested
    @DisplayName("критерий")
    class CriterionBehaviour {

        @Test
        @DisplayName("создаёт балл со своим максимумом и не пускает выше")
        void createsScoreWithinOwnMax() {
            Criterion homework = rubric.addCriterion("ДЗ", 10, WEIGHT);

            assertEquals(new Score(7, 10), homework.score(7));
            assertThrows(InvalidScoreException.class, () -> homework.score(12));
        }

        @Test
        @DisplayName("сущность: равенство по номеру, а не по данным")
        void equalityById() {
            Criterion a = new Criterion(new CriterionId(1), "A", 10, WEIGHT);
            Criterion sameId = new Criterion(new CriterionId(1), "B", 20, WEIGHT);
            Criterion sameData = new Criterion(new CriterionId(2), "A", 10, WEIGHT);

            assertEquals(a, sameId);
            assertNotEquals(a, sameData);
        }
    }
}

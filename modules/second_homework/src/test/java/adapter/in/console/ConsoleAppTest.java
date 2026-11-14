package adapter.in.console;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.StudentView;
import domain.grading.GradeLine;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import support.CourseFixture;
import support.RecordingExporter;
import support.TestConsole;

/**
 * Сценарии меню: ввод подаётся строками, проверяется вывод и состояние
 * курса.
 */
@DisplayName("ConsoleApp — меню")
class ConsoleAppTest {

    private final CourseFixture course = CourseFixture.withRubric();

    private String run(String... lines) {
        TestConsole console = TestConsole.withInput(lines);
        console.app(course).run();
        return console.output();
    }

    @Nested
    @DisplayName("студенты и рубрикатор")
    class Setup {

        @Test
        @DisplayName("добавить студента и посмотреть список")
        void addAndListStudents() {
            String output = run("1", "Анна Смирнова", "2", "0");

            assertTrue(output.contains("Добавлен студент: №1 Анна Смирнова"));
            assertTrue(output.contains("№1 Анна Смирнова, оценок: 0"));
            assertTrue(output.contains("До встречи!"));
        }

        @Test
        @DisplayName("добавить критерий с весом через запятую")
        void addCriterion() {
            String output = run("4", "Проект", "50", "0,25", "5");

            assertTrue(output.contains("Добавлен критерий: №4 Проект (макс. 50, вес 0,25)"));
            assertEquals(4, course.setup().rubric().size());
        }

        @Test
        @DisplayName("ошибка модели показывается, и меню продолжает работу")
        void domainErrorIsShown() {
            String output = run("4", "Экзамен", "10", "1", "2");

            assertTrue(output.contains("Ошибка: Критерий «Экзамен» уже есть"));
            assertTrue(output.contains("Студенты курса:"));
        }

        @Test
        @DisplayName("удалить студента и критерий без оценок")
        void removeStudentAndCriterion() {
            course.setup().addStudent("Анна");

            String output = run("3", "1", "6", "3");

            assertTrue(output.contains("Удалён студент: №1 Анна"));
            assertTrue(output.contains("Удалён критерий: №3 Экзамен"));
            assertTrue(course.setup().students().isEmpty());
        }

        @Test
        @DisplayName("выбирать не из кого — подсказка, а не ошибка")
        void nothingToChoose() {
            String output = run("7");

            assertTrue(output.contains("Сначала добавьте студента (пункт 1)"));
        }
    }

    @Nested
    @DisplayName("оценки")
    class Grading {

        @Test
        @DisplayName("выставить оценку и посмотреть ведомость студента")
        void evaluateAndShowCard() {
            course.setup().addStudent("Анна");

            String output = run("7", "1", "1", "9", "хорошо", "10", "1");

            assertTrue(output.contains(
                    "Выставлено: Домашние задания: 9 из 10, 12.11.2026, «хорошо»"));
            assertTrue(output.contains("Экзамен: —"));
        }

        @Test
        @DisplayName("«12 из 10» отклоняет модель, а не консоль")
        void scoreOutOfRange() {
            course.setup().addStudent("Анна");

            String output = run("7", "1", "1", "12", "");

            assertTrue(output.contains("Ошибка: Балл должен быть от 0 до 10: 12"));
            assertEquals(0, course.setup().students().get(0).evaluationCount());
        }

        @Test
        @DisplayName("исправить и отменить можно только выставленную оценку")
        void correctAndCancel() {
            int anna = course.setup().addStudent("Анна").id().value();
            course.grading().evaluate(anna, 1, 6, "");

            String output = run("8", "1", "1", "10", "пересдал", "9", "1", "1", "9", "1");

            assertTrue(output.contains("Исправлено: Домашние задания: 10 из 10"));
            assertTrue(output.contains("Отменено: Домашние задания: 10 из 10"));
            assertTrue(output.contains("У студента нет выставленных оценок"));
            List<GradeLine> lines = course.grading().card(anna).lines();
            assertTrue(lines.stream().noneMatch(GradeLine::isGraded));
        }
    }

    @Nested
    @DisplayName("итоги")
    class Finals {

        @Test
        @DisplayName("итог студента и ведомость по выбранному правилу")
        void finalGradeAndSheet() {
            StudentView vera = course.setup().addStudent("Вера");
            course.grading().evaluate(vera.id().value(), 1, 4, "");
            course.grading().evaluate(vera.id().value(), 2, 8, "");

            String output = run("11", "1", "2", "12", "1");

            assertTrue(output.contains("Итог: №1 Вера — 4,00 (оценено 2 из 3)"));
            assertTrue(output.contains("правило: текущая успеваемость"));
            assertTrue(output.contains(" 2 (оценено 2 из 3, неуд.)"));
            assertTrue(output.contains("средний итог 2"));
        }

        @Test
        @DisplayName("экспорт идёт через порт; Enter — имя файла по умолчанию")
        void export() {
            course.setup().addStudent("Анна");

            String output = run("13", "1", "");

            assertTrue(output.contains("Ведомость выгружена: " + RecordingExporter.PREFIX
                    + ConsoleApp.DEFAULT_EXPORT_FILE));
            assertEquals(List.of(ConsoleApp.DEFAULT_EXPORT_FILE),
                    course.exporter().destinations());
        }
    }

    @Test
    @DisplayName("неизвестная команда и конец ввода без выхода")
    void unknownCommandAndEndOfInput() {
        String output = run("42");

        assertTrue(output.contains("Нет такого пункта меню: '42'."));
        assertTrue(output.contains("ввод завершён"));
    }

    @Test
    @DisplayName("пункт 14 запускает демонстрацию на том же курсе")
    void demoFromMenu() {
        String output = run("14", "2", "0");

        assertTrue(output.contains("Демонстрация: экспорт итога"));
        assertTrue(output.contains("№1 Анна Смирнова, оценок: 3"));
    }
}

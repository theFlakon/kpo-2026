package adapter.in.console;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.GradeSheet;
import domain.grading.FullRubricPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.CourseFixture;
import support.TestConsole;

@DisplayName("DemoScenario — сквозной сценарий")
class DemoScenarioTest {

    private static long errors(String output) {
        return output.lines().filter(line -> line.contains("Ошибка:")).count();
    }

    @Test
    @DisplayName("проходит все этапы: студенты → критерии → оценки → итог → экспорт")
    void runsEndToEnd() {
        CourseFixture course = CourseFixture.create();
        TestConsole console = TestConsole.withInput();

        console.demo(course).run();

        String output = console.output();
        assertTrue(output.contains("Добавлен студент: №1 Анна Смирнова"));
        assertTrue(output.contains("Добавлен критерий: №3 Экзамен (макс. 100, вес 0,4)"));
        assertTrue(output.contains("Ошибка: Балл должен быть от 0 до 10: 12"));
        assertTrue(output.contains("Ошибка: В рубрикаторе нет критерия №999"));
        assertTrue(output.contains("Итог: №2 Борис Петров — 6 (оценено 3 из 3)"));
        assertTrue(output.contains("Итог: №2 Борис Петров — 7 (оценено 3 из 3)"));
        assertTrue(output.contains("Ведомость выгружена:"));

        GradeSheet exported = course.exporter().sheets().get(0);
        assertEquals(FullRubricPolicy.CODE, exported.policyCode());
        assertEquals("9 7 2", String.join(" ", exported.grades().stream()
                .map(line -> line.grade().value().toPlainString()).toList()));
    }

    @Test
    @DisplayName("повторный запуск на том же курсе даёт те же ошибки, без новых")
    void rerunIsSafe() {
        CourseFixture course = CourseFixture.create();
        TestConsole first = TestConsole.withInput();
        TestConsole second = TestConsole.withInput();

        first.demo(course).run();
        second.demo(course).run();

        assertEquals(errors(first.output()), errors(second.output()));
        assertEquals(3, course.setup().rubric().size());
        assertEquals(6, course.setup().students().size());
    }
}

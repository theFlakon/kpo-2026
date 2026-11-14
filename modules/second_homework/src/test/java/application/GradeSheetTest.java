package application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.grading.FinalGrade;
import domain.student.StudentId;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GradeSheet — аналитика ведомости")
class GradeSheetTest {

    private static StudentGrade grade(int id, String value, int graded) {
        return new StudentGrade(new StudentView(new StudentId(id), "Студент " + id, graded),
                new FinalGrade(new BigDecimal(value), graded, 3));
    }

    private static GradeSheet sheet(StudentGrade... grades) {
        return new GradeSheet("full", "описание", List.of(grades));
    }

    @Test
    @DisplayName("среднее, лучший, худший, неуды и неполные ведомости")
    void analytics() {
        GradeSheet sheet = sheet(grade(1, "9", 3), grade(2, "6", 3), grade(3, "2", 2));

        assertEquals(new BigDecimal("5.67"), sheet.average().orElseThrow());
        assertEquals(new BigDecimal("9"), sheet.best().orElseThrow());
        assertEquals(new BigDecimal("2"), sheet.worst().orElseThrow());
        assertEquals(1, sheet.failingCount());
        assertEquals(1, sheet.incompleteCount());
    }

    @Test
    @DisplayName("среднее округляется арифметически до сотых")
    void averageRounding() {
        assertEquals(new BigDecimal("6.50"),
                sheet(grade(1, "6", 3), grade(2, "7", 3)).average().orElseThrow());
        assertEquals(new BigDecimal("6.67"),
                sheet(grade(1, "6", 3), grade(2, "7", 3), grade(3, "7", 3))
                        .average().orElseThrow());
    }

    @Test
    @DisplayName("пустая ведомость: аналитики нет, а не ноль")
    void emptySheet() {
        GradeSheet empty = sheet();

        assertEquals(Optional.empty(), empty.average());
        assertEquals(Optional.empty(), empty.best());
        assertEquals(Optional.empty(), empty.worst());
        assertEquals(0, empty.failingCount());
    }

    @Test
    @DisplayName("ведомость не меняется вместе с исходным списком")
    void defensiveCopy() {
        List<StudentGrade> source = new ArrayList<>(List.of(grade(1, "9", 3)));
        GradeSheet sheet = new GradeSheet("full", "описание", source);

        source.clear();

        assertEquals(1, sheet.grades().size());
        assertThrows(UnsupportedOperationException.class, () -> sheet.grades().clear());
        assertTrue(new StudentCard(sheet.grades().get(0).student(), List.of())
                .lines().isEmpty());
    }
}

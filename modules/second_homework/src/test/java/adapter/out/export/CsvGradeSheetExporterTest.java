package adapter.out.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.ExportFailedException;
import application.GradeSheet;
import application.StudentGrade;
import application.StudentView;
import domain.grading.FinalGrade;
import domain.student.StudentId;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("CsvGradeSheetExporter — выгрузка в CSV")
class CsvGradeSheetExporterTest {

    private static final String BOM = "\uFEFF";

    private static GradeSheet sheet(String name, String value, int graded) {
        return new GradeSheet("full", "по всему рубрикатору",
                List.of(new StudentGrade(new StudentView(new StudentId(1), name, graded),
                        new FinalGrade(new BigDecimal(value), graded, 3))));
    }

    private static List<String> read(String path) throws IOException {
        String text = Files.readString(Path.of(path), StandardCharsets.UTF_8);
        assertTrue(text.startsWith(BOM), "метка UTF-8 для Excel");
        return List.of(text.substring(BOM.length()).split("\r\n"));
    }

    @Test
    @DisplayName("заголовок с правилом, строка студента, десятичная запятая")
    void writesTable(@TempDir Path directory) throws IOException {
        String path = new CsvGradeSheetExporter(directory)
                .export(sheet("Анна Смирнова", "6.73", 2), "final-grades.csv");

        assertEquals(directory.resolve("final-grades.csv").toAbsolutePath().toString(),
                path);
        assertEquals(List.of(
                "№;Студент;Итог (по всему рубрикатору);Оценено критериев;"
                        + "Всего критериев;Положительный",
                "1;Анна Смирнова;6,73;2;3;да"), read(path));
    }

    @Test
    @DisplayName("значения с «;» и кавычками экранируются по RFC 4180")
    void escapesSpecialCharacters(@TempDir Path directory) throws IOException {
        String path = new CsvGradeSheetExporter(directory)
                .export(sheet("Анна; \"староста\"", "3", 3), "f.csv");

        assertEquals("1;\"Анна; \"\"староста\"\"\";3;3;3;нет", read(path).get(1));
    }

    @Test
    @DisplayName("недостающие каталоги создаются")
    void createsDirectories(@TempDir Path directory) {
        String path = new CsvGradeSheetExporter(directory.resolve("a/b"))
                .export(sheet("Анна", "9", 3), "c/grades.csv");

        assertTrue(Files.isRegularFile(Path.of(path)));
    }

    @Test
    @DisplayName("ошибка файловой системы — ошибка сценария с причиной")
    void failureBecomesExportFailed(@TempDir Path directory) throws IOException {
        Files.createDirectory(directory.resolve("busy"));
        CsvGradeSheetExporter exporter = new CsvGradeSheetExporter(directory);

        ExportFailedException error = assertThrows(ExportFailedException.class,
                () -> exporter.export(sheet("Анна", "9", 3), "busy"));

        assertTrue(error.getCause() instanceof IOException);
    }

    @Test
    @DisplayName("недопустимое имя файла — ошибка сценария, а не падение")
    void invalidFileName(@TempDir Path directory) {
        CsvGradeSheetExporter exporter = new CsvGradeSheetExporter(directory);

        // Символ NUL недопустим в имени файла в любой ОС.
        ExportFailedException error = assertThrows(ExportFailedException.class,
                () -> exporter.export(sheet("Анна", "9", 3), "bad\u0000.csv"));

        assertTrue(error.getCause() instanceof InvalidPathException);
    }
}

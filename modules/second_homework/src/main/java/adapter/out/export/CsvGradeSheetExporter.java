package adapter.out.export;

import application.ExportFailedException;
import application.GradeSheet;
import application.GradeSheetExporter;
import application.StudentGrade;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;

/**
 * Выгрузка ведомости в CSV для учебного офиса.
 *
 * <p>Формат подобран под русский Excel: разделитель «;», десятичная
 * запятая и метка порядка байтов UTF-8 — без неё Excel показывает
 * кириллицу кракозябрами. Значения с разделителем, кавычкой или
 * переводом строки экранируются по RFC 4180.
 */
public final class CsvGradeSheetExporter implements GradeSheetExporter {

    /** Разделитель полей. */
    private static final String SEPARATOR = ";";

    /** Метка порядка байтов UTF-8. */
    private static final char BYTE_ORDER_MARK = '\uFEFF';

    /** Кавычка для экранирования. */
    private static final String QUOTE = "\"";

    /** Каталог, относительно которого разрешается имя файла. */
    private final Path directory;

    /**
     * Создаёт адаптер.
     *
     * @param directory каталог для выгрузок; создаётся при первой выгрузке
     */
    public CsvGradeSheetExporter(@NonNull Path directory) {
        this.directory = directory;
    }

    /**
     * {@inheritDoc}
     *
     * @param destination имя файла относительно каталога выгрузок
     *                    или абсолютный путь
     * @return абсолютный путь к файлу
     */
    @Override
    public String export(@NonNull GradeSheet sheet,
                         @NonNull String destination) {
        Path target = resolve(destination);
        try {
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Writer writer = Files.newBufferedWriter(target,
                    StandardCharsets.UTF_8)) {
                writer.write(BYTE_ORDER_MARK);
                writer.write(row(List.of("№", "Студент",
                        "Итог (" + sheet.policyDescription() + ")",
                        "Оценено критериев", "Всего критериев",
                        "Положительный")));
                for (StudentGrade grade : sheet.grades()) {
                    writer.write(row(cells(grade)));
                }
            }
        } catch (IOException e) {
            throw new ExportFailedException(target.toString(), e);
        }
        return target.toString();
    }

    /**
     * Превращает введённое пользователем имя в путь.
     *
     * @param destination имя файла или путь
     * @return абсолютный путь
     * @throws ExportFailedException если имя недопустимо в этой файловой
     *                               системе, например содержит «:» в Windows
     */
    private Path resolve(String destination) {
        try {
            return directory.resolve(destination).toAbsolutePath();
        } catch (InvalidPathException e) {
            // Имя вводит пользователь: недопустимое имя — ошибка сценария,
            // а не падение приложения.
            throw new ExportFailedException(destination, e);
        }
    }

    /**
     * Ячейки строки одного студента.
     *
     * @param grade итог студента
     * @return значения ячеек
     */
    private static List<String> cells(StudentGrade grade) {
        return List.of(
                String.valueOf(grade.student().id().value()),
                grade.student().fullName(),
                decimal(grade.grade().value()),
                String.valueOf(grade.grade().gradedCriteria()),
                String.valueOf(grade.grade().totalCriteria()),
                grade.grade().isPassing() ? "да" : "нет");
    }

    /**
     * Собирает строку CSV.
     *
     * @param cells значения ячеек
     * @return строка с переводом строки в конце
     */
    private static String row(List<String> cells) {
        // RFC 4180 требует CRLF; так же файл ожидает и Excel.
        return cells.stream()
                .map(CsvGradeSheetExporter::escape)
                .collect(Collectors.joining(SEPARATOR)) + "\r\n";
    }

    /**
     * Экранирует значение ячейки.
     *
     * @param value значение
     * @return значение как есть или в кавычках с удвоенными кавычками
     */
    private static String escape(String value) {
        boolean needsQuotes = value.contains(SEPARATOR)
                || value.contains(QUOTE)
                || value.contains("\n")
                || value.contains("\r");
        if (!needsQuotes) {
            return value;
        }
        return QUOTE + value.replace(QUOTE, QUOTE + QUOTE) + QUOTE;
    }

    /**
     * Записывает число с десятичной запятой.
     *
     * @param value число
     * @return запись вида «7,45»
     */
    private static String decimal(BigDecimal value) {
        return value.toPlainString().replace('.', ',');
    }
}

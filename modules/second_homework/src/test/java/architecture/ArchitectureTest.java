package architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Зависимости направлены внутрь: проверка по импортам исходников.
 *
 * <p>Без сторонних библиотек: импорт — единственный способ сослаться
 * на класс другого пакета (полные имена в коде не используются),
 * поэтому разбора строк {@code import} достаточно.
 */
@DisplayName("Архитектура: зависимости направлены внутрь")
class ArchitectureTest {

    /** Корень исходников; Gradle и IDE запускают тесты из каталога модуля. */
    private static final Path SOURCES = Path.of("src", "main", "java");

    /**
     * Разрешённый импорт из java.io: {@code @Serial} — аннотация для
     * компилятора у {@code serialVersionUID} исключений, а не ввод-вывод.
     */
    private static final String SERIAL = "import java.io.Serial;";

    private static List<String> importsOf(String layer) throws IOException {
        try (Stream<Path> files = Files.walk(SOURCES.resolve(layer))) {
            return files.filter(path -> path.toString().endsWith(".java"))
                    .flatMap(ArchitectureTest::lines)
                    .filter(line -> line.startsWith("import "))
                    .toList();
        }
    }

    private static Stream<String> lines(Path file) {
        try {
            return Files.readAllLines(file).stream().map(String::strip);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @ParameterizedTest(name = "{0} не зависит от {1}")
    @CsvSource({
        "domain,      application",
        "domain,      adapter",
        "domain,      config",
        "domain,      org.springframework",
        "domain,      java.io",
        "domain,      java.nio",
        "application, adapter",
        "application, config",
        "application, org.springframework",
        "application, java.io",
        "application, java.nio",
        "adapter,     config",
        "adapter,     org.springframework"
    })
    @DisplayName("внутренний слой не знает о внешних и о фреймворке")
    void innerLayerDoesNotDependOnOuter(String layer, String forbidden)
            throws IOException {
        List<String> violations = importsOf(layer).stream()
                .filter(line -> line.startsWith("import " + forbidden + ".")
                        || line.startsWith("import static " + forbidden + "."))
                .filter(line -> !line.equals(SERIAL))
                .toList();

        assertEquals(List.of(), violations);
    }

    @Test
    @DisplayName("проверка действительно видит исходники")
    void sourcesAreFound() throws IOException {
        assertFalse(importsOf("domain").isEmpty());
        assertFalse(importsOf("application").isEmpty());
    }
}

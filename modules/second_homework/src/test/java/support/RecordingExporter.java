package support;

import application.GradeSheet;
import application.GradeSheetExporter;
import java.util.ArrayList;
import java.util.List;

/**
 * Подменная выгрузка: ничего не пишет на диск, а запоминает вызовы.
 *
 * <p>Совмещает stub (фиксированный ответ) и spy (запись вызовов).
 */
public final class RecordingExporter implements GradeSheetExporter {

    /** Префикс ответа: по нему видно, что выгрузка была подменной. */
    public static final String PREFIX = "записано в память: ";

    /** Выгруженные ведомости в порядке вызовов. */
    private final List<GradeSheet> sheets = new ArrayList<>();

    /** Места выгрузки в порядке вызовов. */
    private final List<String> destinations = new ArrayList<>();

    @Override
    public String export(GradeSheet sheet, String destination) {
        sheets.add(sheet);
        destinations.add(destination);
        return PREFIX + destination;
    }

    /**
     * Ведомости, которые передали на выгрузку.
     *
     * @return неизменяемая копия списка
     */
    public List<GradeSheet> sheets() {
        return List.copyOf(sheets);
    }

    /**
     * Места выгрузки, которые передали вместе с ведомостями.
     *
     * @return неизменяемая копия списка
     */
    public List<String> destinations() {
        return List.copyOf(destinations);
    }
}

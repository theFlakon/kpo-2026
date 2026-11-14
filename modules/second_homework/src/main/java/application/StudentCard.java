package application;

import domain.grading.GradeLine;
import java.util.List;
import lombok.NonNull;

/**
 * Оценки студента по всем критериям рубрикатора.
 *
 * @param student студент
 * @param lines   строки ведомости в порядке рубрикатора, с оценкой или без
 */
public record StudentCard(@NonNull StudentView student,
                          @NonNull List<GradeLine> lines) {

    /**
     * Делает список строк неизменяемым.
     */
    public StudentCard {
        lines = List.copyOf(lines);
    }
}

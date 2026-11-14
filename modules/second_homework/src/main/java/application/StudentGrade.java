package application;

import domain.grading.FinalGrade;
import lombok.NonNull;

/**
 * Итог одного студента — строка итоговой ведомости.
 *
 * @param student студент
 * @param grade   итог по выбранному правилу
 */
public record StudentGrade(@NonNull StudentView student,
                           @NonNull FinalGrade grade) {
}

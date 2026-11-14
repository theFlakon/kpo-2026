package application;

import domain.student.Student;
import domain.student.StudentId;
import lombok.NonNull;

/**
 * Студент для списка: номер, ФИО и число оценок.
 *
 * <p>Неизменяемая проекция агрегата {@link Student}: внешний слой видит
 * данные, но не может выставить оценку в обход сценария.
 *
 * @param id              номер студента
 * @param fullName        ФИО
 * @param evaluationCount число выставленных оценок
 */
public record StudentView(@NonNull StudentId id, @NonNull String fullName,
                          int evaluationCount) {

    /**
     * Строит проекцию студента.
     *
     * @param student студент
     * @return проекция
     */
    public static StudentView of(@NonNull Student student) {
        return new StudentView(student.getId(), student.getFullName(),
                student.evaluations().size());
    }
}

package domain.exception;

import java.io.Serial;
import lombok.Getter;

/**
 * Студента с таким номером нет на курсе.
 */
@Getter
public final class UnknownStudentException extends PosException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /** Запрошенный номер студента. */
    private final int studentId;

    /**
     * Создаёт исключение.
     *
     * @param studentId запрошенный номер студента
     */
    public UnknownStudentException(int studentId) {
        super("Нет студента №" + studentId);
        this.studentId = studentId;
    }
}

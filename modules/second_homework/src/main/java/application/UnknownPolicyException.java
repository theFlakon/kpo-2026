package application;

import domain.exception.PosException;
import java.io.Serial;
import lombok.Getter;

/**
 * Правила итога с таким кодом нет среди настроенных.
 */
@Getter
public final class UnknownPolicyException extends PosException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /** Запрошенный код. */
    private final String code;

    /**
     * Создаёт исключение.
     *
     * @param code запрошенный код
     */
    public UnknownPolicyException(String code) {
        super("Нет правила итога «" + code + "»");
        this.code = code;
    }
}

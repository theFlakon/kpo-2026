package domain.exception;

import java.io.Serial;
import lombok.Getter;

/**
 * Код вида транспорта или вещи не зарегистрирован в фабрике.
 *
 * <p>Бросается до техосмотра и до выдачи инвентарного номера,
 * поэтому неизвестный вид не тратит номер.
 */
@Getter
public final class UnknownTypeCodeException extends VyshKatException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /** Отклонённый код; может быть {@code null}. */
    private final String code;

    /**
     * Создаёт исключение для неизвестного кода.
     *
     * @param code отклонённый код, допускается {@code null}
     */
    public UnknownTypeCodeException(String code) {
        super("Неизвестный вид: '" + code + "'");
        this.code = code;
    }
}

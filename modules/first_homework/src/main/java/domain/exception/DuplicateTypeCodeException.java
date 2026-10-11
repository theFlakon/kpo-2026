package domain.exception;

import java.io.Serial;
import lombok.Getter;

/**
 * Код вида зарегистрирован в фабрике повторно.
 *
 * <p>Ошибка конфигурации, а не пользовательских данных: регистрация
 * происходит в {@code AppConfig}, поэтому исключение проявится сразу
 * при запуске приложения.
 */
@Getter
public final class DuplicateTypeCodeException extends VyshKatException {

    /** Версия формы сериализации. */
    @Serial
    private static final long serialVersionUID = 1L;

    /** Код, который уже зарегистрирован. */
    private final String code;

    /**
     * Создаёт исключение для повторного кода.
     *
     * @param code код, который уже зарегистрирован
     */
    public DuplicateTypeCodeException(String code) {
        super("Вид с кодом '" + code + "' уже зарегистрирован");
        this.code = code;
    }
}
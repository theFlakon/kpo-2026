package domain.inspection;

import domain.exception.InvalidInspectionResultException;

/**
 * Решение техосмотра по заявке.
 *
 * <p>Отказ — нормальный исход, а не исключение: отклонённая техника
 * является штатным бизнес-сценарием.
 *
 * <p>Создавать следует через {@link #pass()} и {@link #fail(String)}.
 * Причина есть у любого решения, поэтому консоль всегда может объяснить,
 * почему технику приняли или отклонили.
 *
 * @param passed пройден ли техосмотр
 * @param reason непустое объяснение решения
 */
public record InspectionResult(boolean passed, String reason) {

    /** Причина по умолчанию для одобренной заявки. */
    private static final String DEFAULT_PASS_REASON = "Техосмотр пройден";

    /**
     * Проверяет, что у решения есть причина.
     *
     * @throws InvalidInspectionResultException если причина пустая
     */
    public InspectionResult {
        if (reason == null || reason.isBlank()) {
            throw new InvalidInspectionResultException();
        }
    }

    /**
     * Создаёт решение «техосмотр пройден».
     *
     * @return одобряющее решение со стандартной причиной
     */
    public static InspectionResult pass() {
        return new InspectionResult(true, DEFAULT_PASS_REASON);
    }

    /**
     * Создаёт решение «техосмотр не пройден».
     *
     * @param reason непустая причина отказа
     * @return отклоняющее решение
     * @throws InvalidInspectionResultException если причина пустая
     */
    public static InspectionResult fail(String reason) {
        return new InspectionResult(false, reason);
    }
}
package support;

import domain.inspection.InspectionResult;
import domain.inspection.IntakeRequest;
import domain.inspection.ServiceCenter;
import java.util.ArrayList;
import java.util.List;

/**
 * Подменный техосмотр: всегда возвращает заранее заданное решение
 * и запоминает, какие заявки ему передали.
 *
 * <p>Совмещает stub (фиксированный ответ) и spy (запись вызовов).
 * Для простых случаев хватает лямбды, для проверки «осмотр не вызывался»
 * подходит mock из Mockito — в тестах показаны все три способа.
 */
public final class StubServiceCenter implements ServiceCenter {

    /** Решение, которое возвращается на любую заявку. */
    private final InspectionResult answer;

    /** Заявки в порядке поступления. */
    private final List<IntakeRequest> inspected = new ArrayList<>();

    private StubServiceCenter(InspectionResult answer) {
        this.answer = answer;
    }

    /**
     * Техосмотр, который пропускает всё.
     *
     * @return подменный техосмотр
     */
    public static StubServiceCenter passing() {
        return new StubServiceCenter(InspectionResult.pass());
    }

    /**
     * Техосмотр, который всё отклоняет.
     *
     * @param reason причина отказа
     * @return подменный техосмотр
     */
    public static StubServiceCenter failing(String reason) {
        return new StubServiceCenter(InspectionResult.fail(reason));
    }

    @Override
    public InspectionResult inspect(IntakeRequest request) {
        inspected.add(request);
        return answer;
    }

    /**
     * Заявки, которые получил техосмотр.
     *
     * @return неизменяемая копия списка
     */
    public List<IntakeRequest> inspected() {
        return List.copyOf(inspected);
    }
}

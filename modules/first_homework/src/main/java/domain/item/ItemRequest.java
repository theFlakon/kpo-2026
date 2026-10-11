package domain.item;

import domain.capability.EnergyConsumer;
import domain.capability.Inventoriable;
import lombok.Builder;

/**
 * Заявка на постановку вещи на баланс.
 *
 * <p>Симметрична {@code IntakeRequest} для транспорта, но без данных
 * техосмотра: вещи его не проходят. Некорректная заявка не может
 * существовать, поэтому исключение в конструкторе вещи не случится
 * уже после выдачи инвентарного номера.
 *
 * <p>Код вида здесь не проверяется: это задача {@code ItemFactory}.
 *
 * @param itemType       код вида вещи, например {@code "helmet"}
 * @param name           непустое название
 * @param dailyEnergyKwh суточный расход, кВт·ч; для вещей, не потребляющих
 *                       энергию, игнорируется и по умолчанию равен 0
 */
// Builder вызывает канонический конструктор, проверки не обходятся.
@Builder
public record ItemRequest(
        String itemType,
        String name,
        double dailyEnergyKwh) {

    /**
     * Проверяет поля заявки.
     *
     * @throws domain.exception.InvalidAssetDataException
     *         если название или расход нарушают контракт
     */
    public ItemRequest {
        Inventoriable.requireValidName(name);
        EnergyConsumer.requireValidEnergy(dailyEnergyKwh);
    }
}

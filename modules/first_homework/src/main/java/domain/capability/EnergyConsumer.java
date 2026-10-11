package domain.capability;

import domain.exception.InvalidEnergyConsumptionException;

/**
 * Устройство, которое потребляет электроэнергию.
 *
 * <p>Роль ортогональна иерархиям транспорта и вещей: её реализуют
 * электротранспорт и часть вещей (док-станции, зарядные шкафы),
 * но не обычный велосипед и не шлем.
 *
 * <p>Классы, которые энергию не потребляют, этот интерфейс
 * реализовывать не должны: возврат нуля «для совместимости»
 * размыл бы смысл роли и сломал бы отчёты, которые отбирают
 * потребителей по типу.
 */
public interface EnergyConsumer {

    /**
     * Возвращает суточный расход электроэнергии.
     *
     * <p>Единица измерения зашита в имя метода, чтобы при суммировании
     * разных устройств не возникало вопроса, в чём считаем.
     *
     * @return расход в кВт·ч за сутки, неотрицательное конечное число
     */
    double getDailyEnergyKwh();

    /**
     * Проверяет значение суточного расхода на соответствие контракту.
     *
     * @param dailyEnergyKwh проверяемый расход, кВт·ч
     * @return то же значение, если оно корректно
     * @throws InvalidEnergyConsumptionException если значение
     *         отрицательно, бесконечно или {@code NaN}
     */
    static double requireValidEnergy(double dailyEnergyKwh) {
        // Сравнение NaN < 0 даёт false, поэтому одного "< 0" мало:
        // NaN прошёл бы проверку и отравил бы суммарный отчёт.
        if (!Double.isFinite(dailyEnergyKwh) || dailyEnergyKwh < 0) {
            throw new InvalidEnergyConsumptionException(dailyEnergyKwh);
        }
        return dailyEnergyKwh;
    }
}
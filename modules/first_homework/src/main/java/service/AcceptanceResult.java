package service;

import domain.inspection.InspectionResult;
import domain.vehicle.Vehicle;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Итог приёма техники: решение техосмотра и принятый транспорт.
 *
 * <p>Инвариант: транспорт есть тогда и только тогда, когда техосмотр
 * пройден. Его гарантирует закрытый конструктор — создать результат
 * можно только через {@link #accepted} и {@link #rejected}.
 */
// Обычный класс, а не record: канонический конструктор записи нельзя
// сделать закрытым, и инвариант пришлось бы проверять исключением.
// Заодно Optional остаётся только в возвращаемом значении, а не в поле.
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public final class AcceptanceResult {

    /** Решение техосмотра; при отказе содержит причину. */
    @Getter
    private final InspectionResult inspection;

    /** Принятый транспорт; {@code null}, если техосмотр не пройден. */
    private final Vehicle vehicle;

    /**
     * Создаёт результат успешного приёма.
     *
     * @param inspection пройденный техосмотр
     * @param vehicle    принятый и сохранённый транспорт
     * @return результат с транспортом
     */
    public static AcceptanceResult accepted(
            @NonNull InspectionResult inspection, @NonNull Vehicle vehicle) {
        return new AcceptanceResult(inspection, vehicle);
    }

    /**
     * Создаёт результат отказа.
     *
     * @param inspection непройденный техосмотр с причиной
     * @return результат без транспорта
     */
    public static AcceptanceResult rejected(
            @NonNull InspectionResult inspection) {
        return new AcceptanceResult(inspection, null);
    }

    /**
     * Сообщает, принята ли техника.
     *
     * @return {@code true}, если техосмотр пройден и транспорт в парке
     */
    public boolean isAccepted() {
        return vehicle != null;
    }

    /**
     * Возвращает принятый транспорт.
     *
     * @return транспорт или пустой {@code Optional} при отказе
     */
    public Optional<Vehicle> getVehicle() {
        return Optional.ofNullable(vehicle);
    }
}
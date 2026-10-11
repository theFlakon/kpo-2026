package service;

import domain.vehicle.Vehicle;
import lombok.NonNull;
import lombok.ToString;

/**
 * Базовое правило из задания: транспорт подходит новичку,
 * если его простота не ниже порога.
 */
@ToString
public final class SimplicityThresholdPolicy implements BeginnerPolicy {

    /** Порог из задания: простота от 6 и выше. */
    public static final int DEFAULT_THRESHOLD = 6;

    /**
     * Минимальная простота, при которой транспорт подходит новичку.
     */
    private final int threshold;

    /**
     * Создаёт правило с заданным порогом.
     *
     * @param threshold минимальная простота, от 1 до 10
     * @throws domain.exception.InvalidSimplicityException
     *         если порог вне шкалы простоты
     */
    public SimplicityThresholdPolicy(int threshold) {
        // Порог — тоже значение простоты, поэтому и правило проверки то же.
        this.threshold = Vehicle.requireValidSimplicity(threshold);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Граница включительна: при пороге 6 транспорт с простотой 6
     * подходит.
     */
    @Override
    public boolean isSuitableForBeginner(@NonNull Vehicle vehicle) {
        return vehicle.getSimplicity() >= threshold;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String description() {
        return "простота ≥ " + threshold;
    }
}
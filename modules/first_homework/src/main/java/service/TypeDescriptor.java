package service;

import lombok.NonNull;

/**
 * Описание зарегистрированного вида транспорта или вещи.
 *
 * <p>Нужно консоли, чтобы построить меню выбора из реестра фабрики:
 * новый вид появится в меню без правки консоли.
 *
 * @param code           код вида в заявке, например {@code "scooter"}
 * @param displayName    название для пользователя, например «Электросамокат»
 * @param consumesEnergy потребляет ли вид энергию, то есть нужно ли
 *                       спрашивать суточный расход
 */
public record TypeDescriptor(
        @NonNull String code,
        @NonNull String displayName,
        boolean consumesEnergy) {
}

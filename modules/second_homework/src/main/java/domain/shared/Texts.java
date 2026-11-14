package domain.shared;

import domain.exception.InvalidTextException;

/**
 * Проверка текстовых полей модели: ФИО, названий, комментариев.
 *
 * <p>Правило «непустой текст разумной длины» нужно нескольким понятиям
 * языка предметной области. Оно записано один раз, а предельную длину
 * задаёт каждое понятие само.
 */
public final class Texts {

    /**
     * Закрытый конструктор: класс содержит только статические методы.
     */
    private Texts() {
    }

    /**
     * Проверяет обязательный текст.
     *
     * @param value     проверяемое значение
     * @param field     название поля для сообщения об ошибке
     * @param maxLength предельная длина без пробелов по краям
     * @return значение без пробелов по краям
     * @throws InvalidTextException если значение пустое или длиннее предела
     */
    public static String requireText(String value, String field,
                                     int maxLength) {
        if (value == null || value.isBlank()) {
            throw InvalidTextException.blank(field);
        }
        String trimmed = value.strip();
        if (trimmed.length() > maxLength) {
            throw InvalidTextException.tooLong(field, maxLength);
        }
        return trimmed;
    }

    /**
     * Проверяет необязательный текст.
     *
     * @param value     проверяемое значение, допускается {@code null}
     * @param field     название поля для сообщения об ошибке
     * @param maxLength предельная длина без пробелов по краям
     * @return значение без пробелов по краям или пустая строка,
     *         если значение не задано
     * @throws InvalidTextException если значение длиннее предела
     */
    public static String optionalText(String value, String field,
                                      int maxLength) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return requireText(value, field, maxLength);
    }
}

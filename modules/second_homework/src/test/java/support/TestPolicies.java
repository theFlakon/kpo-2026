package support;

import domain.grading.FullRubricPolicy;
import domain.grading.GradedOnlyPolicy;
import domain.grading.GradeLine;
import domain.grading.GradingPolicy;
import domain.grading.RoundedPolicy;
import java.math.BigDecimal;
import java.util.List;

/**
 * Правила итога для тестов.
 */
public final class TestPolicies {

    /**
     * Закрытый конструктор: класс содержит только статические методы.
     */
    private TestPolicies() {
    }

    /**
     * Правила, как в конфигурации приложения: официальный итог до целого,
     * текущая успеваемость до сотых.
     *
     * @return список правил
     */
    public static List<GradingPolicy> configured() {
        return List.of(new RoundedPolicy(new FullRubricPolicy(), 0),
                new RoundedPolicy(new GradedOnlyPolicy(), 2));
    }

    /**
     * Правило-заглушка: всегда возвращает заданное число и запоминает,
     * сколько строк ему передали.
     *
     * @param code  код правила
     * @param value возвращаемый итог
     * @return заглушка
     */
    public static Fixed fixed(String code, String value) {
        return new Fixed(code, new BigDecimal(value));
    }

    /**
     * Правило-заглушка с фиксированным ответом.
     */
    public static final class Fixed implements GradingPolicy {

        /** Код правила. */
        private final String code;

        /** Возвращаемый итог. */
        private final BigDecimal value;

        /** Сколько строк передали при последнем вызове; -1 — не вызывали. */
        private int lastLineCount = -1;

        /**
         * Создаёт заглушку.
         *
         * @param code  код правила
         * @param value возвращаемый итог
         */
        Fixed(String code, BigDecimal value) {
            this.code = code;
            this.value = value;
        }

        @Override
        public String code() {
            return code;
        }

        @Override
        public String description() {
            return "заглушка «" + code + "»";
        }

        @Override
        public BigDecimal grade(List<GradeLine> lines) {
            lastLineCount = lines.size();
            return value;
        }

        /**
         * Сколько строк ведомости получило правило при последнем вызове.
         *
         * @return число строк или -1, если правило не вызывали
         */
        public int lastLineCount() {
            return lastLineCount;
        }
    }
}

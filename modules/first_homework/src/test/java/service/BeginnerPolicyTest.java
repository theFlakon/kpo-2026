package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import domain.capability.EnergyConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.TestData;

/**
 * Интерфейс правила остаётся функциональным.
 */
@DisplayName("BeginnerPolicy")
class BeginnerPolicyTest {

    @Test
    @DisplayName("лямбда получает описание по умолчанию")
    void lambdaGetsDefaultDescription() {
        BeginnerPolicy policy = vehicle -> true;

        assertEquals("по правилу парка", policy.description());
    }

    @Test
    @DisplayName("любое правило можно задать лямбдой")
    void anyRuleAsLambda() {
        BeginnerPolicy onlyElectric =
                vehicle -> vehicle instanceof EnergyConsumer;

        assertFalse(onlyElectric.isSuitableForBeginner(TestData.bicycle(1, 10)));
    }
}

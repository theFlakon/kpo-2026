package config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.inspection.ServiceCenter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import service.AcceptanceResult;
import service.FleetService;
import service.ReportService;
import support.StubServiceCenter;
import support.TestData;

/**
 * Подмена техосмотра через DI-контейнер — критерий задания.
 *
 * <p>Контекст собирается из настоящей {@link AppConfig} и маленькой
 * тестовой конфигурации с бином {@code @Primary}. Ни FleetService,
 * ни AppConfig о подмене не знают: контейнер сам подставляет fake,
 * потому что FleetService получает техосмотр по типу через параметр.
 */
@DisplayName("Подмена ServiceCenter в контейнере")
class ServiceCenterOverrideTest {

    /** Причина отказа подменного техосмотра. */
    private static final String STUB_REASON = "тестовый техосмотр";

    /**
     * Тестовая конфигурация: техосмотр, который всё отклоняет.
     */
    @Configuration(proxyBeanMethods = false)
    static class RejectingInspection {

        @Bean
        @Primary
        ServiceCenter rejectingServiceCenter() {
            return StubServiceCenter.failing(STUB_REASON);
        }
    }

    @Test
    @DisplayName("FleetService получает подменный техосмотр")
    void fleetServiceUsesOverriddenServiceCenter() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(
                             AppConfig.class, RejectingInspection.class)) {
            // Заявка идеальна: настоящий техосмотр её бы принял.
            AcceptanceResult result = context.getBean(FleetService.class)
                    .acceptVehicle(TestData.intake().build());

            assertFalse(result.isAccepted());
            assertEquals(STUB_REASON, result.getInspection().reason());
        }
    }

    @Test
    @DisplayName("подменён только техосмотр: остальной граф настоящий")
    void onlyServiceCenterIsReplaced() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(
                             AppConfig.class, RejectingInspection.class)) {
            FleetService fleet = context.getBean(FleetService.class);

            // Вещи техосмотр не проходят — значит, ставятся как обычно.
            fleet.registerItem(TestData.item().build());

            assertEquals(1, context.getBean(ReportService.class).countAll());
            assertTrue(fleet.availableVehicleTypes().size() > 0);
        }
    }
}

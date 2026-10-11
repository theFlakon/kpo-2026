package config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cli.ConsoleApp;
import cli.DemoScenario;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import repository.InventoryRepository;
import service.BeginnerPolicy;
import service.FleetService;
import service.ReportService;
import service.TypeDescriptor;
import support.TestData;

/**
 * Граф зависимостей собирается настоящим контейнером Spring.
 */
@DisplayName("AppConfig")
class AppConfigTest {

    private AnnotationConfigApplicationContext context;

    @BeforeEach
    void startContext() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @Test
    @DisplayName("контекст поднимается, консоль собрана")
    void contextStarts() {
        context.getBean(ConsoleApp.class);
    }

    @Test
    @DisplayName("приём и отчёты работают с одним хранилищем (синглтон)")
    void servicesShareRepository() {
        context.getBean(FleetService.class)
                .registerItem(TestData.item().build());

        assertEquals(1, context.getBean(ReportService.class).countAll());
        assertSame(context.getBean(InventoryRepository.class),
                context.getBean(InventoryRepository.class));
    }

    @Test
    @DisplayName("коды видов совпадают с TestData")
    void typeCodesMatchTestData() {
        FleetService fleet = context.getBean(FleetService.class);

        assertEquals(List.of(TestData.SCOOTER, TestData.BICYCLE,
                TestData.E_BIKE), codes(fleet.availableVehicleTypes()));
        assertEquals(List.of(TestData.HELMET, TestData.DOCK,
                TestData.CABINET), codes(fleet.availableItemTypes()));
    }

    private static List<String> codes(List<TypeDescriptor> types) {
        return types.stream().map(TypeDescriptor::code).toList();
    }

    @Test
    @DisplayName("правило для новичков из задания: простота ≥ 6")
    void beginnerPolicyFromAssignment() {
        assertEquals("простота ≥ 6",
                context.getBean(BeginnerPolicy.class).description());
    }

    @Test
    @DisplayName("настоящий техосмотр: порог 70 % включительно")
    void realServiceCenterThreshold() {
        FleetService fleet = context.getBean(FleetService.class);

        assertTrue(fleet.acceptVehicle(TestData.intake().technicalCondition(70)
                .build()).isAccepted());
        assertFalse(fleet.acceptVehicle(TestData.intake().technicalCondition(69)
                .build()).isAccepted());
    }

    @Test
    @DisplayName("демонстрация из контекста работает с тем же парком")
    void demoUsesSharedPark() {
        context.getBean(DemoScenario.class).run();

        assertTrue(context.getBean(ReportService.class).countAll() > 0);
    }
}

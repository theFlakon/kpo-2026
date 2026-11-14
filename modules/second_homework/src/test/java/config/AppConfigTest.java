package config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import adapter.in.console.ConsoleApp;
import application.CourseSetupService;
import application.FinalGradeService;
import domain.grading.FullRubricPolicy;
import domain.grading.GradedOnlyPolicy;
import domain.grading.GradingPolicy;
import domain.student.StudentRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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
    @DisplayName("все сценарии работают с одним хранилищем (синглтон)")
    void servicesShareRepositories() {
        context.getBean(CourseSetupService.class).addStudent("Анна");
        context.getBean(CourseSetupService.class)
                .addCriterion("Экзамен", 100, BigDecimal.ONE);

        assertEquals(1, context.getBean(FinalGradeService.class)
                .sheet(FullRubricPolicy.CODE).grades().size());
        assertSame(context.getBean(StudentRepository.class),
                context.getBean(StudentRepository.class));
    }

    @Test
    @DisplayName("правила итога: официальный до целого, текущий до сотых")
    void configuredPolicies() {
        List<GradingPolicy> policies = context.getBean(FinalGradeService.class)
                .policies();

        assertEquals(List.of(FullRubricPolicy.CODE, GradedOnlyPolicy.CODE),
                policies.stream().map(GradingPolicy::code).toList());
        assertEquals(0, policies.get(0).grade(List.of()).scale());
        assertEquals(2, policies.get(1).grade(List.of()).scale());
    }
}

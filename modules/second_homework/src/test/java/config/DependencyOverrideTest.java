package config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.CourseSetupService;
import application.FinalGradeService;
import application.GradeSheetExporter;
import application.GradingService;
import domain.grading.FullRubricPolicy;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import support.RecordingExporter;
import support.TestData;

/**
 * Подмена зависимостей через DI-контейнер.
 *
 * <p>Контекст собирается из настоящей {@link AppConfig} и маленькой
 * тестовой конфигурации с бинами {@code @Primary}: фиксированные часы
 * и выгрузка в память. Ни сценарии, ни AppConfig о подмене не знают:
 * контейнер подставляет её, потому что зависимости приходят по типу
 * порта.
 */
@DisplayName("Подмена часов и выгрузки в контейнере")
class DependencyOverrideTest {

    /**
     * Тестовая конфигурация: подменяет только два порта.
     */
    @Configuration(proxyBeanMethods = false)
    static class Overrides {

        @Bean
        @Primary
        Clock fixedClock() {
            return TestData.CLOCK;
        }

        @Bean
        @Primary
        GradeSheetExporter recordingExporter() {
            return new RecordingExporter();
        }
    }

    @Test
    @DisplayName("дата оценки — из подменных часов, выгрузка — в память, а не на диск")
    void servicesUseOverriddenPorts() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class, Overrides.class)) {
            CourseSetupService setup = context.getBean(CourseSetupService.class);
            int anna = setup.addStudent("Анна").id().value();
            int exam = setup.addCriterion("Экзамен", 100, BigDecimal.ONE).getId().value();

            assertEquals(TestData.TODAY, context.getBean(GradingService.class)
                    .evaluate(anna, exam, 80, "").getEvaluation().orElseThrow().getDate());

            String result = context.getBean(FinalGradeService.class)
                    .export(FullRubricPolicy.CODE, "f.csv");
            RecordingExporter exporter =
                    (RecordingExporter) context.getBean(GradeSheetExporter.class);
            assertTrue(result.startsWith(RecordingExporter.PREFIX));
            assertEquals(List.of("f.csv"), exporter.destinations());
        }
    }
}

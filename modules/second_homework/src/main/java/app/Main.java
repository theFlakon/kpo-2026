package app;

import adapter.in.console.ConsoleApp;
import adapter.in.console.DemoScenario;
import config.AppConfig;
import java.util.Arrays;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Точка входа «ПОС».
 *
 * <p>Без аргументов запускает интерактивное меню. С аргументом
 * {@value #DEMO_FLAG} прогоняет сквозной сценарий и завершается —
 * удобно для быстрой проверки.
 */
public final class Main {

    /** Аргумент запуска демонстрации. */
    private static final String DEMO_FLAG = "--demo";

    /**
     * Закрытый конструктор: класс содержит только точку входа.
     */
    private Main() {
    }

    /**
     * Поднимает DI-контейнер и запускает консоль.
     *
     * @param args {@value #DEMO_FLAG} для демонстрации без меню
     */
    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {
            if (Arrays.asList(args).contains(DEMO_FLAG)) {
                context.getBean(DemoScenario.class).run();
            } else {
                context.getBean(ConsoleApp.class).run();
            }
        }
    }
}

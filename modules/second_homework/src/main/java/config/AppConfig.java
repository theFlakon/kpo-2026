package config;

import adapter.in.console.ConsoleApp;
import adapter.in.console.ConsoleInput;
import adapter.in.console.ConsolePrinter;
import adapter.in.console.DemoScenario;
import adapter.out.export.CsvGradeSheetExporter;
import adapter.out.persistence.InMemoryRubricRepository;
import adapter.out.persistence.InMemoryStudentRepository;
import application.CourseSetupService;
import application.FinalGradeService;
import application.GradeSheetExporter;
import application.GradingService;
import domain.grading.FinalGradeCalculator;
import domain.grading.FullRubricPolicy;
import domain.grading.GradedOnlyPolicy;
import domain.grading.GradingPolicy;
import domain.grading.RoundedPolicy;
import domain.rubric.RubricRepository;
import domain.student.EvaluationFactory;
import domain.student.StudentRepository;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import java.util.Scanner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация DI-контейнера: весь граф зависимостей в одном месте.
 *
 * <p>Классы приложения не помечены аннотациями Spring — бины объявлены
 * здесь явно. Поэтому домен и сценарии не зависят от фреймворка, а граф
 * виден целиком.
 *
 * <p>Все бины — синглтоны (поведение Spring по умолчанию). Для хранилищ
 * это критично: второй экземпляр разделил бы курс надвое.
 *
 * <p>Бины, у которых есть абстракция (порт), объявлены типом интерфейса:
 * остальной граф знает только её, и тест подменяет именно её.
 */
@Configuration(proxyBeanMethods = false)
public final class AppConfig {

    /** Системное свойство с каталогом выгрузок. */
    public static final String EXPORT_DIR_PROPERTY = "pos.export.dir";

    /** Каталог выгрузок, если свойство не задано. */
    private static final String DEFAULT_EXPORT_DIR = "exports";

    /** Знаков после запятой у текущей успеваемости. */
    private static final int CURRENT_SCALE = 2;

    /**
     * Системные часы — источник даты оценки.
     *
     * @return часы в часовом поясе системы
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    /**
     * Хранилище студентов в памяти.
     *
     * @return единственное хранилище студентов
     */
    @Bean
    public StudentRepository studentRepository() {
        return new InMemoryStudentRepository();
    }

    /**
     * Хранилище рубрикатора в памяти.
     *
     * @return единственное хранилище рубрикатора
     */
    @Bean
    public RubricRepository rubricRepository() {
        return new InMemoryRubricRepository();
    }

    /**
     * Фабрика оценок.
     *
     * @param clock источник даты оценки
     * @return фабрика
     */
    @Bean
    public EvaluationFactory evaluationFactory(Clock clock) {
        return new EvaluationFactory(clock);
    }

    /**
     * Доменный сервис расчёта итога.
     *
     * @return калькулятор итога
     */
    @Bean
    public FinalGradeCalculator finalGradeCalculator() {
        return new FinalGradeCalculator();
    }

    /**
     * Выгрузка ведомости в CSV.
     *
     * <p>Каталог берётся из системного свойства {@value #EXPORT_DIR_PROPERTY}:
     * задача {@code run} в Gradle направляет выгрузки в {@code build/}.
     *
     * @return адаптер выгрузки
     */
    @Bean
    public GradeSheetExporter gradeSheetExporter() {
        return new CsvGradeSheetExporter(Path.of(System.getProperty(
                EXPORT_DIR_PROPERTY, DEFAULT_EXPORT_DIR)));
    }

    /**
     * Сценарии «студенты и рубрикатор».
     *
     * @param students хранилище студентов
     * @param rubrics  хранилище рубрикатора
     * @return сервис
     */
    @Bean
    public CourseSetupService courseSetupService(StudentRepository students,
                                                 RubricRepository rubrics) {
        return new CourseSetupService(students, rubrics);
    }

    /**
     * Сценарии «оценки».
     *
     * @param students    хранилище студентов
     * @param rubrics     хранилище рубрикатора
     * @param evaluations фабрика оценок
     * @param calculator  калькулятор итога
     * @return сервис
     */
    @Bean
    public GradingService gradingService(StudentRepository students,
                                         RubricRepository rubrics,
                                         EvaluationFactory evaluations,
                                         FinalGradeCalculator calculator) {
        return new GradingService(students, rubrics, evaluations, calculator);
    }

    /**
     * Сценарии «итоги».
     *
     * @param students   хранилище студентов
     * @param rubrics    хранилище рубрикатора
     * @param calculator калькулятор итога
     * @param exporter   выгрузка ведомости
     * @return сервис
     */
    @Bean
    public FinalGradeService finalGradeService(StudentRepository students,
                                               RubricRepository rubrics,
                                               FinalGradeCalculator calculator,
                                               GradeSheetExporter exporter) {
        return new FinalGradeService(students, rubrics, calculator,
                gradingPolicies(), exporter);
    }

    /**
     * Вывод в стандартный поток.
     *
     * @return форматирование вывода
     */
    @Bean
    public ConsolePrinter consolePrinter() {
        return new ConsolePrinter(System.out);
    }

    /**
     * Ввод из стандартного потока.
     *
     * @return чтение ввода
     */
    @Bean
    public ConsoleInput consoleInput() {
        return new ConsoleInput(new Scanner(System.in), System.out);
    }

    /**
     * Сквозной сценарий.
     *
     * @param setup   сценарии «студенты и рубрикатор»
     * @param grading сценарии «оценки»
     * @param finals  сценарии «итоги»
     * @param printer форматирование вывода
     * @return демонстрация
     */
    @Bean
    public DemoScenario demoScenario(CourseSetupService setup,
                                     GradingService grading,
                                     FinalGradeService finals,
                                     ConsolePrinter printer) {
        return new DemoScenario(setup, grading, finals, printer);
    }

    /**
     * Консольное приложение.
     *
     * @param setup   сценарии «студенты и рубрикатор»
     * @param grading сценарии «оценки»
     * @param finals  сценарии «итоги»
     * @param input   чтение ввода
     * @param printer форматирование вывода
     * @param demo    сквозной сценарий
     * @return консольное приложение
     */
    @Bean
    public ConsoleApp consoleApp(CourseSetupService setup,
                                 GradingService grading,
                                 FinalGradeService finals, ConsoleInput input,
                                 ConsolePrinter printer, DemoScenario demo) {
        return new ConsoleApp(setup, grading, finals, input, printer, demo);
    }

    /**
     * Правила итога курса; первое — официальное.
     *
     * <p>Декоратор округления надевается здесь, а не внутри правил:
     * официальный итог по шкале ВШЭ — целое число, текущая успеваемость
     * информативнее с сотыми.
     *
     * @return правила в порядке показа пользователю
     */
    static List<GradingPolicy> gradingPolicies() {
        return List.of(
                new RoundedPolicy(new FullRubricPolicy(), 0),
                new RoundedPolicy(new GradedOnlyPolicy(), CURRENT_SCALE));
    }
}

package application;

import domain.exception.RemovalBlockedException;
import domain.rubric.Criterion;
import domain.rubric.CriterionId;
import domain.rubric.Rubric;
import domain.rubric.RubricRepository;
import domain.rubric.Weight;
import domain.student.Student;
import domain.student.StudentId;
import domain.student.StudentRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Сценарии «студенты и рубрикатор»: кто учится на курсе и по каким
 * критериям оценивают.
 */
@RequiredArgsConstructor
public final class CourseSetupService {

    /** Хранилище студентов. */
    @NonNull
    private final StudentRepository students;

    /** Хранилище рубрикатора. */
    @NonNull
    private final RubricRepository rubrics;

    /**
     * Добавляет студента на курс.
     *
     * @param fullName ФИО
     * @return добавленный студент
     * @throws domain.exception.InvalidTextException если ФИО некорректно
     */
    public StudentView addStudent(String fullName) {
        Student student = new Student(students.nextId(), fullName);
        students.save(student);
        return StudentView.of(student);
    }

    /**
     * Возвращает студентов курса.
     *
     * @return неизменяемый список в порядке добавления
     */
    public List<StudentView> students() {
        return students.findAll().stream().map(StudentView::of).toList();
    }

    /**
     * Удаляет студента без оценок.
     *
     * @param studentId номер студента
     * @return удалённый студент
     * @throws domain.exception.UnknownStudentException если студента нет
     * @throws RemovalBlockedException если у студента есть оценки
     */
    public StudentView removeStudent(int studentId) {
        Student student = students.get(new StudentId(studentId));
        student.ensureRemovable();
        students.remove(student.getId());
        return StudentView.of(student);
    }

    /**
     * Добавляет критерий в рубрикатор.
     *
     * @param name     название
     * @param maxScore максимальный балл
     * @param weight   вклад в итог
     * @return добавленный критерий
     * @throws domain.exception.InvalidValueException
     *         если название, максимум или вес некорректны
     * @throws domain.exception.DuplicateCriterionException
     *         если название занято
     */
    public Criterion addCriterion(String name, int maxScore,
                                  @NonNull BigDecimal weight) {
        Rubric rubric = rubrics.get();
        Criterion criterion = rubric.addCriterion(name, maxScore,
                new Weight(weight));
        rubrics.save(rubric);
        return criterion;
    }

    /**
     * Возвращает критерии рубрикатора.
     *
     * @return неизменяемый список в порядке добавления
     */
    public List<Criterion> rubric() {
        return rubrics.get().criteria();
    }

    /**
     * Удаляет критерий, по которому нет оценок.
     *
     * <p>Проверка затрагивает два агрегата — рубрикатор и всех студентов,
     * — поэтому живёт в сценарии, а не в одном из них.
     *
     * @param criterionId номер критерия
     * @return удалённый критерий
     * @throws domain.exception.UnknownCriterionException
     *         если критерия нет
     * @throws RemovalBlockedException если по критерию есть оценки
     */
    public Criterion removeCriterion(int criterionId) {
        Rubric rubric = rubrics.get();
        CriterionId id = new CriterionId(criterionId);
        Criterion criterion = rubric.criterion(id);
        if (students.findAll().stream()
                .anyMatch(student -> student.hasEvaluationFor(id))) {
            throw new RemovalBlockedException(
                    "критерий «" + criterion.getName() + "»");
        }
        rubric.removeCriterion(id);
        rubrics.save(rubric);
        return criterion;
    }
}

package application;

/**
 * Выгрузка итоговой ведомости — выходной порт слоя приложения.
 *
 * <p>Сценарий экспорта решает, что выгрузить, а адаптер — в каком
 * формате и куда. Поэтому слой приложения не знает про CSV и файлы,
 * а в тестах порт подменяется лямбдой.
 */
@FunctionalInterface
public interface GradeSheetExporter {

    /**
     * Выгружает ведомость.
     *
     * @param sheet       итоговая ведомость
     * @param destination куда выгрузить, например имя файла; смысл
     *                    определяет адаптер
     * @return описание результата для пользователя, например путь к файлу
     * @throws ExportFailedException если выгрузить не удалось
     */
    String export(GradeSheet sheet, String destination);
}

package backend.academy.scrapper.service;

public interface LinkChecker {
    /**
     * Проверяет, есть ли обновления для указанной ссылки.
     *
     * @param link Ссылка для проверки.
     * @return true, если есть обновления, иначе false.
     */
    boolean checkForUpdates(String link);

    /**
     * Возвращает описание обновления (если оно есть).
     *
     * @param link Ссылка.
     * @return Описание обновления.
     */
    String getUpdateDescription(String link);
}

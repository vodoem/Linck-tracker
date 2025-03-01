package backend.academy.scrapper.service;

public interface LinkChecker {

    boolean checkForUpdates(String link);

    String getUpdateDescription(String link);
}

package backend.academy.scrapper.model;

import java.util.List;

public record LinkUpdate(long id, String url, String description, List<Long> tgChatIds) {}

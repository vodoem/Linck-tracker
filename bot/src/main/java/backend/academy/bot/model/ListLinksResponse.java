package backend.academy.bot.model;

import java.util.List;

public record ListLinksResponse(
    List<LinkResponse> links,
    int size
) {}

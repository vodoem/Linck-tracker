package backend.academy.model;

public record GetTagsForLinkRequest(String correlationId, long chatId, String url) {}


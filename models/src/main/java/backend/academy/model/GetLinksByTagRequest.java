package backend.academy.model;

public record GetLinksByTagRequest(String correlationId, long chatId, String tagName) {}

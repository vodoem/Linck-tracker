package backend.academy.model;

public record KafkaRemoveTagRequest(String correlationId, long chatId, String url, String tagName) {}

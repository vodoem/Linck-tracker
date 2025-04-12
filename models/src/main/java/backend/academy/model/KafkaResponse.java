package backend.academy.model;

public record KafkaResponse(String correlationId, Object data) {}

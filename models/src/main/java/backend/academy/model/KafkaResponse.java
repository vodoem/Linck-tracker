package backend.academy.model;

public sealed interface KafkaResponse permits KafkaLinksResponse, KafkaTagsResponse {
    String correlationId();
}

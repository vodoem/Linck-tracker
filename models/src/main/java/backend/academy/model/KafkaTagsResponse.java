package backend.academy.model;

import java.util.List;

public record KafkaTagsResponse(String correlationId, List<String> tags) implements KafkaResponse {}

package backend.academy.model;

import java.util.List;

public record KafkaLinksResponse(String correlationId, List<LinkResponse> links) {}

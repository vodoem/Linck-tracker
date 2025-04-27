package backend.academy.model;

import java.util.List;

public record KafkaAddTagsRequest(String correlationId, long chatId, String url, List<String> tags) {}

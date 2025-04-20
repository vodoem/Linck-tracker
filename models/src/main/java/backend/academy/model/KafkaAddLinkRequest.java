package backend.academy.model;

import java.util.List;

public record KafkaAddLinkRequest(long chatId, String link, List<String> tags, List<String> filters) {}

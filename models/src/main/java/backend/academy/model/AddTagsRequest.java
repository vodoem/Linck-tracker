package backend.academy.model;

import java.util.List;

public record AddTagsRequest(String url, List<String> tags) {}

package backend.academy.model;

import java.util.List;

public record GetLinksResponse(String correlationId, List<LinkResponse> links) {
}

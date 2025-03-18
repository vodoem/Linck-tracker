package backend.academy.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record LinkResponse(long id, String url, List<String> tags, List<String> filters) {
}

package backend.academy.bot.exceptionhandler;

import backend.academy.model.ApiErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.NativeWebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<?> handleHttpClientError(HttpClientErrorException ex, NativeWebRequest request) {
        try {
            // Преобразуем тело ошибки в ApiErrorResponse
            ApiErrorResponse errorResponse =
                    objectMapper.readValue(ex.getResponseBodyAsString(), ApiErrorResponse.class);
            return buildResponse(ex.getStatusCode(), errorResponse, request);
        } catch (Exception e) {
            // Если не удалось преобразовать, создаем стандартный ответ
            ApiErrorResponse error = new ApiErrorResponse(
                    "Ошибка клиента",
                    String.valueOf(ex.getStatusCode().value()),
                    ex.getClass().getName(),
                    ex.getMessage(),
                    Arrays.asList(Arrays.toString(e.getStackTrace())));
            return buildResponse(ex.getStatusCode(), error, request);
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex, NativeWebRequest request) {
        ApiErrorResponse error = new ApiErrorResponse(
                "Внутренняя ошибка сервера",
                "500",
                ex.getClass().getName(),
                ex.getMessage(),
                Arrays.asList(Arrays.toString(ex.getStackTrace())));
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, error, request);
    }

    private ResponseEntity<?> buildResponse(HttpStatusCode status, ApiErrorResponse body, NativeWebRequest request) {
        if (isSseRequest(request)) {
            return buildSseResponse(status, body);
        }
        return ResponseEntity.status(status).body(body);
    }

    private boolean isSseRequest(NativeWebRequest request) {
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        return accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE);
    }

    private ResponseEntity<String> buildSseResponse(HttpStatusCode status, ApiErrorResponse body) {
        return ResponseEntity.status(status)
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(formatSsePayload(body));
    }

    private String formatSsePayload(ApiErrorResponse body) {
        String json = safelyWriteJson(body);
        return "event: error\n" + "data: " + json + "\n\n";
    }

    private String safelyWriteJson(ApiErrorResponse body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            return '{' + "\"description\":\"" + body.description() + "\"}";
        }
    }
}

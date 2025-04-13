package backend.academy.bot.exceptionhandler;

import backend.academy.model.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpClientError(HttpClientErrorException ex) {
        try {
            // Преобразуем тело ошибки в ApiErrorResponse
            ApiErrorResponse errorResponse =
                    objectMapper.readValue(ex.getResponseBodyAsString(), ApiErrorResponse.class);
            return new ResponseEntity<>(errorResponse, ex.getStatusCode());
        } catch (Exception e) {
            // Если не удалось преобразовать, создаем стандартный ответ
            ApiErrorResponse error = new ApiErrorResponse(
                    "Ошибка клиента",
                    String.valueOf(ex.getStatusCode().value()),
                    ex.getClass().getName(),
                    ex.getMessage(),
                    Arrays.asList(Arrays.toString(e.getStackTrace())));
            return new ResponseEntity<>(error, ex.getStatusCode());
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception ex) {
        ApiErrorResponse error = new ApiErrorResponse(
                "Внутренняя ошибка сервера",
                "500",
                ex.getClass().getName(),
                ex.getMessage(),
                Arrays.asList(Arrays.toString(ex.getStackTrace())));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

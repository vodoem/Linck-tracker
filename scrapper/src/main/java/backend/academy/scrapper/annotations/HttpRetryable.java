package backend.academy.scrapper.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.HttpServerErrorException;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Retryable(
        maxAttemptsExpression = "${http.retry.max-attempts}",
        backoff = @Backoff(delayExpression = "${http.retry.backoff-ms}"),
        retryFor = {HttpServerErrorException.class},
        noRetryFor = {IllegalArgumentException.class} // Пример: не повторять для некорректных аргументов
        )
public @interface HttpRetryable {}

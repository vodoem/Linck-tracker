package backend.academy.bot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Retryable(
        maxAttemptsExpression = "${http.retry.max-attempts}",
        backoff = @Backoff(delayExpression = "${http.retry.backoff-ms}"),
        retryFor = {HttpServerErrorException.class},
        noRetryFor = {IllegalArgumentException.class, HttpClientErrorException.class})
public @interface HttpRetryable {}

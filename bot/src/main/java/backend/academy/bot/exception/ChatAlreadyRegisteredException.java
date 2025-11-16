package backend.academy.bot.exception;

/**
 * Исключение, сигнализирующее о том, что Telegram-чат уже был зарегистрирован ранее
 * и повторный вызов /start должен обрабатываться идемпотентно.
 */
public class ChatAlreadyRegisteredException extends RuntimeException {

    public ChatAlreadyRegisteredException(String message) {
        super(message);
    }

    public ChatAlreadyRegisteredException(String message, Throwable cause) {
        super(message, cause);
    }
}

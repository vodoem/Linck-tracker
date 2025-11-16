package backend.academy.bot.config;

import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardManager;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Встроенный Tomcat по умолчанию пытается восстанавливать сериализованные HTTP-сессии
 * из файла {@code SESSIONS.ser}. После изменений в моделях/классах такие данные нередко
 * оказываются несовместимыми и Tomcat выбрасывает {@link java.io.StreamCorruptedException}.
 * <p>
 * Чтобы не получать эту ошибку при каждом старте devtools, отключаем дисковую
 * сериализацию сессий, явно обнуляя путь до файла сохранения.
 */
@Configuration
public class TomcatSessionConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> disableSessionPersistence() {
        return factory -> factory.addContextCustomizers(context -> {
            Manager manager = context.getManager();
            if (manager instanceof StandardManager standardManager) {
                // null вместо "SESSIONS.ser" полностью отключает восстановление сессий из файла
                standardManager.setPathname(null);
            }
        });
    }
}


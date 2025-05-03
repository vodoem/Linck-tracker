package backend.academy.bot.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.bot.BotInitializer;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;

@SpringBootTest
@AutoConfigureWireMock(port = 8081)
public class ScrapperClientTest {

    @Autowired
    private ScrapperClient scrapperClient;

    @MockitoBean
    BotInitializer botInitializer;

    @Test
    void shouldRetryOnRetryableCodes() {
        // Настройка WireMock для возврата ошибки 500 два раза, затем успешного ответа
        stubFor(post(urlEqualTo("/tg-chat/12345"))
                .willReturn(aResponse().withStatus(500))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs(Scenario.STARTED)
                .willSetStateTo("First Attempt"));

        stubFor(post(urlEqualTo("/tg-chat/12345"))
                .willReturn(aResponse().withStatus(500))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("First Attempt")
                .willSetStateTo("Second Attempt"));

        stubFor(post(urlEqualTo("/tg-chat/12345"))
                .willReturn(aResponse().withStatus(200))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("Second Attempt"));

        // Вызов метода с Retry
        scrapperClient.registerChat(12345L);

        // Проверка, что запрос был выполнен трижды
        verify(exactly(3), postRequestedFor(urlEqualTo("/tg-chat/12345")));
    }

    @Test
    void shouldNotRetryOnNonRetryableCodes() {
        // Настройка WireMock для возврата ошибки 404
        stubFor(post(urlEqualTo("/tg-chat/123456")).willReturn(aResponse().withStatus(404)));

        // Вызов метода с Retry
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            scrapperClient.registerChat(123456L);
        });

        assertEquals(404, exception.getStatusCode().value());

        // Проверка, что запрос был выполнен только один раз
        verify(exactly(1), postRequestedFor(urlEqualTo("/tg-chat/123456")));
    }

    @Test
    void shouldOpenCircuitBreakerAfterFailures() {
        // Настройка WireMock для возврата ошибки 500 трижды
        stubFor(post(urlEqualTo("/tg-chat/1234567")).willReturn(aResponse().withStatus(500)));

        // Первые два вызова приводят к ошибкам
        RuntimeException exception1 = assertThrows(RuntimeException.class, () -> {
            scrapperClient.registerChat(1234567L);
        });

        // Третий вызов также завершается ошибкой, CB переходит в OPEN
        RuntimeException cbException = assertThrows(RuntimeException.class, () -> {
            scrapperClient.registerChat(1234567L);
        });

        assertTrue(cbException.getMessage().contains("Сервис временно недоступен."));
    }
}

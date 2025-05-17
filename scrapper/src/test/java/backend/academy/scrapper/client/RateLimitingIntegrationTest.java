package backend.academy.scrapper.client;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.scrapper.repository.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest(properties = {"app.message-transport=HTTP"})
@AutoConfigureMockMvc
public class RateLimitingIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${resilience4j.ratelimiter.instances.linkControllerRateLimiter.limitForPeriod}")
    private int maxRequestsPerMinute; // Лимит из конфигурации

    @Test
    void shouldReturn429WhenRateLimitExceeded() throws Exception {
        // Arrange
        String endpoint = "/links"; // URL эндпоинта в Scrapper
        long chatId = 12345L; // Пример ID чата
        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(chatId));

        // Act-Assert
        for (int i = 0; i < maxRequestsPerMinute; i++) {
            mockMvc.perform(MockMvcRequestBuilders.get(endpoint).headers(headers))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(MockMvcRequestBuilders.get(endpoint).headers(headers)).andExpect(status().isTooManyRequests());
    }
}

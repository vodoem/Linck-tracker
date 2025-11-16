package backend.academy.scrapper.fallback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.repository.AbstractIntegrationTest;
import backend.academy.scrapper.service.DynamicCommunicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
public class TransportManagerTest extends AbstractIntegrationTest {

    @MockitoBean
    private TransportHealthChecker healthChecker;

    @Autowired
    private DynamicCommunicationService dynamicCommunicationService;

    @Autowired
    private TransportManager transportManager;

    @Test
    void shouldSwitchToHttpWhenKafkaIsDown() {
        // Arrange
        when(healthChecker.isKafkaAvailable()).thenReturn(false); // Kafka недоступна
        when(healthChecker.isHttpAvailable()).thenReturn(true); // HTTP доступен
        dynamicCommunicationService.setCurrentTransport("Kafka");

        // Act
        transportManager.checkAndSwitchTransport();

        // Assert
        assertEquals("HTTP", dynamicCommunicationService.getCurrentTransport());
    }

    @Test
    void shouldSwitchToKafkaWhenHttpIsDown() {
        // Arrange
        when(healthChecker.isHttpAvailable()).thenReturn(false); // HTTP недоступен
        when(healthChecker.isKafkaAvailable()).thenReturn(true); // Kafka доступен
        dynamicCommunicationService.setCurrentTransport("HTTP");

        // Act
        transportManager.checkAndSwitchTransport();

        // Assert
        assertEquals("Kafka", dynamicCommunicationService.getCurrentTransport());
    }

    @Test
    void shouldReturnToPreferredTransportWhenItBecomesAvailable() {
        // Arrange
        when(healthChecker.isHttpAvailable()).thenReturn(true);
        when(healthChecker.isKafkaAvailable()).thenReturn(true);
        dynamicCommunicationService.setCurrentTransport("Kafka");

        // Act
        transportManager.checkAndSwitchTransport();

        // Assert
        assertEquals("HTTP", dynamicCommunicationService.getCurrentTransport());
    }
}

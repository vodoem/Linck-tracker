package backend.academy.bot.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class BotServiceTest {
    private final BotService botService = new BotService(null, null);

    @Test
    void testValidUrl() {
        // Act & Assert
        assertTrue(botService.isValidUrl("https://example.com"));
        assertTrue(botService.isValidUrl("http://example.com"));
        assertTrue(botService.isValidUrl("www.example.com"));
    }

    @Test
    void testInvalidUrl() {
        // Act & Assert
        assertFalse(botService.isValidUrl("invalid-url"));
        assertFalse(botService.isValidUrl("ftp://example.com"));
        assertFalse(botService.isValidUrl(""));
    }
}

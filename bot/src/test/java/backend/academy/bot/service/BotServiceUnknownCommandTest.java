package backend.academy.bot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class BotServiceUnknownCommandTest {
    private final BotService botService = new BotService(null, null, null, null);

    @Test
    void testUnknownCommand() {
        // Arrange
        long chatId = 12345L;
        String unknownCommand = "/unknown";

        // Act
        String response = botService.handleCommand(unknownCommand, chatId);

        // Assert
        assertEquals("Неизвестная команда. Используйте /help для просмотра доступных команд.", response);
    }
}

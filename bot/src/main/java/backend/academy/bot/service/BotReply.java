package backend.academy.bot.service;

import com.pengrad.telegrambot.model.request.Keyboard;

public record BotReply(String text, Keyboard keyboard) {}

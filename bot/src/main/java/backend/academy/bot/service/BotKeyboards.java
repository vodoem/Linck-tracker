package backend.academy.bot.service;

import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;

public final class BotKeyboards {
    public static final String TRACK = "➕ Добавить ссылку";
    public static final String LIST = "📋 Мои ссылки";
    public static final String UNTRACK = "➖ Удалить ссылку";
    public static final String TAGS = "🏷 Теги";
    public static final String SETTINGS = "🔔 Уведомления";
    public static final String HELP = "❓ Помощь";

    public static final String ADD_TAGS = "➕ Добавить теги";
    public static final String LIST_TAGS = "📎 Теги ссылки";
    public static final String FILTER_BY_TAG = "🔎 Найти по тегу";
    public static final String REMOVE_TAG = "🗑 Удалить тег";

    public static final String IMMEDIATE_MODE = "⚡ Сразу";
    public static final String DIGEST_MODE = "🗞 Дайджест";

    public static final String SKIP = "⏭ Пропустить";
    public static final String CANCEL = "❌ Отмена";
    public static final String BACK = "⬅️ Назад";

    private BotKeyboards() {}

    public static ReplyKeyboardMarkup mainMenu() {
        return keyboard(new String[] {TRACK, LIST}, new String[] {UNTRACK, TAGS}, new String[] {SETTINGS, HELP});
    }

    public static ReplyKeyboardMarkup tagsMenu() {
        return keyboard(
                new String[] {ADD_TAGS, LIST_TAGS}, new String[] {FILTER_BY_TAG, REMOVE_TAG}, new String[] {BACK});
    }

    public static ReplyKeyboardMarkup settingsMenu() {
        return keyboard(new String[] {IMMEDIATE_MODE, DIGEST_MODE}, new String[] {BACK});
    }

    public static ReplyKeyboardMarkup inputMenu() {
        return keyboard(new String[] {CANCEL});
    }

    public static ReplyKeyboardMarkup optionalInputMenu() {
        return keyboard(new String[] {SKIP, CANCEL});
    }

    private static ReplyKeyboardMarkup keyboard(String[]... rows) {
        return new ReplyKeyboardMarkup(rows).resizeKeyboard(true).isPersistent(true);
    }
}

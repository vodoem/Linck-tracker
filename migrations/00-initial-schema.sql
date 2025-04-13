-- Таблица для Telegram-чатов
CREATE TABLE tg_chat (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Таблица отслеживаемых ссылок
CREATE TABLE tracked_link (
    id SERIAL PRIMARY KEY,
    url TEXT UNIQUE NOT NULL,
    chat_id BIGINT REFERENCES tg_chat(id) ON DELETE CASCADE,
    last_checked TIMESTAMP DEFAULT NOW(),
    CONSTRAINT unique_url_per_chat UNIQUE (url, chat_id)
);

-- Таблица тегов
CREATE TABLE tag (
    id SERIAL PRIMARY KEY,
    link_id INT REFERENCES tracked_link(id) ON DELETE CASCADE,
    name TEXT NOT NULL
);

-- Таблица фильтров
CREATE TABLE filter (
    id SERIAL PRIMARY KEY,
    link_id INT REFERENCES tracked_link(id) ON DELETE CASCADE,
    value TEXT NOT NULL
);

-- Индексы
CREATE INDEX idx_tracked_link_chat_id ON tracked_link(chat_id);
CREATE INDEX idx_tag_link_id ON tag(link_id);
CREATE INDEX idx_filter_link_id ON filter(link_id);

-- Таблица для Telegram-чатов
CREATE TABLE tg_chat (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Таблица отслеживаемых ссылок
CREATE TABLE tracked_link (
    id SERIAL PRIMARY KEY,
    url TEXT NOT NULL,
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

-- Проектная таблица состояния отслеживаемого ресурса
CREATE TABLE tracked_link_state (
    tracked_link_state_id BIGSERIAL PRIMARY KEY,
    tracked_link_id BIGINT NOT NULL,
    last_event_id TEXT,
    last_activity_at TIMESTAMP,
    state_hash TEXT,
    state_payload JSONB,
    checked_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_tracked_link_state_link
        FOREIGN KEY (tracked_link_id)
        REFERENCES tracked_link(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_tracked_link_state_link UNIQUE (tracked_link_id)
);

-- Индексы
CREATE INDEX idx_tracked_link_chat_id ON tracked_link(chat_id);
CREATE INDEX idx_tag_link_id ON tag(link_id);
CREATE INDEX idx_filter_link_id ON filter(link_id);
CREATE INDEX idx_tracked_link_state_tracked_link_id ON tracked_link_state(tracked_link_id);

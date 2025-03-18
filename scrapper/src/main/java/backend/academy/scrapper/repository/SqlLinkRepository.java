package backend.academy.scrapper.repository;

import backend.academy.model.LinkResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class SqlLinkRepository implements LinkRepository {
    private final JdbcTemplate jdbcTemplate;

    public SqlLinkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addLink(long chatId, String url, List<String> tags, List<String> filters) {
        // Проверяем, существует ли ссылка
        boolean exists = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM tracked_link WHERE url = ? AND chat_id = ?",
            Integer.class,
            url, chatId
        ) > 0;

        if (exists) {
            throw new IllegalArgumentException("Ссылка уже отслеживается.");
        }

        // Добавляем ссылку
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO tracked_link (url, chat_id) VALUES (?, ?) RETURNING id",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, url);
            ps.setLong(2, chatId);
            return ps;
        }, keyHolder);

        int linkId = Objects.requireNonNull(keyHolder.getKey()).intValue();

        // Добавляем теги
        if (!tags.isEmpty()) {
            List<Object[]> tagArgs = tags.stream()
                .map(tag -> new Object[]{linkId, tag})
                .collect(Collectors.toList());
            jdbcTemplate.batchUpdate(
                "INSERT INTO tag (link_id, name) VALUES (?, ?)",
                tagArgs
            );
        }

        // Добавляем фильтры
        if (!filters.isEmpty()) {
            List<Object[]> filterArgs = filters.stream()
                .map(filter -> new Object[]{linkId, filter})
                .collect(Collectors.toList());
            jdbcTemplate.batchUpdate(
                "INSERT INTO filter (link_id, value) VALUES (?, ?)",
                filterArgs
            );
        }
    }

    @Override
    public void removeLink(long chatId, String url) {
        jdbcTemplate.update(
            "DELETE FROM tracked_link WHERE url = ? AND chat_id = ?",
            url, chatId
        );
    }

    @Override
    public List<LinkResponse> getLinks(long chatId, int offset, int limit) {
        String sql = """
        SELECT tl.id, tl.url,
               ARRAY_AGG(DISTINCT t.name) FILTER (WHERE t.name IS NOT NULL) AS tags,
               ARRAY_AGG(DISTINCT f.value) FILTER (WHERE f.value IS NOT NULL) AS filters
        FROM tracked_link tl
        LEFT JOIN tag t ON tl.id = t.link_id
        LEFT JOIN filter f ON tl.id = f.link_id
        WHERE tl.chat_id = ?
        GROUP BY tl.id, tl.url
        ORDER BY tl.id
        OFFSET ? LIMIT ?
    """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            int id = rs.getInt("id");
            String url = rs.getString("url");
            Array tagsArray = rs.getArray("tags");
            Array filtersArray = rs.getArray("filters");

            List<String> tags = tagsArray != null
                ? Arrays.asList((String[]) tagsArray.getArray())
                : Collections.emptyList();

            List<String> filters = filtersArray != null
                ? Arrays.asList((String[]) filtersArray.getArray())
                : Collections.emptyList();

            return new LinkResponse(id, url, tags, filters);
        }, chatId, offset, limit);
    }

    @Override
    public void registerChat(long chatId) {
        jdbcTemplate.update(
            "INSERT INTO tg_chat (id) VALUES (?) ON CONFLICT DO NOTHING",
            chatId
        );
    }

    @Override
    public void deleteChat(long chatId) {
        jdbcTemplate.update("DELETE FROM tg_chat WHERE id = ?", chatId);
    }

    @Override
    public List<Long> getAllChatIds() {
        return jdbcTemplate.queryForList("SELECT id FROM tg_chat", Long.class);
    }
}

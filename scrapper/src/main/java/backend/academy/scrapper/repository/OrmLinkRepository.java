package backend.academy.scrapper.repository;

import backend.academy.model.LinkResponse;
import backend.academy.scrapper.repository.DTO.Filter;
import backend.academy.scrapper.repository.DTO.Tag;
import backend.academy.scrapper.repository.DTO.TgChat;
import backend.academy.scrapper.repository.DTO.TrackedLink;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class OrmLinkRepository implements LinkRepository {
    private final EntityManager em;

    public OrmLinkRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    @Transactional
    public void addLink(long chatId, String url, List<String> tags, List<String> filters) {
        TrackedLink link = em.createQuery(
                        "SELECT tl FROM TrackedLink tl WHERE tl.chat.id = :chatId AND tl.url = :url", TrackedLink.class)
                .setParameter("chatId", chatId)
                .setParameter("url", url)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (link != null) {
            throw new IllegalArgumentException("Ссылка уже отслеживается.");
        }

        // Создание новой ссылки
        TgChat chat = em.find(TgChat.class, chatId);
        if (chat == null) {
            chat = new TgChat();
            chat.setId(chatId);
            em.persist(chat);
        }

        TrackedLink newLink = new TrackedLink();
        newLink.setUrl(url);
        newLink.setChat(chat);
        newLink.setLastChecked(LocalDateTime.now());

        tags.forEach(tagName -> {
            Tag tag = new Tag();
            tag.setName(tagName);
            tag.setLink(newLink);
            newLink.getTags().add(tag);
        });

        filters.forEach(filterValue -> {
            Filter filter = new Filter();
            filter.setValue(filterValue);
            filter.setLink(newLink);
            newLink.getFilters().add(filter);
        });

        em.persist(newLink);
    }

    @Override
    @Transactional
    public void removeLink(long chatId, String url) {
        TrackedLink link = em.createQuery(
                        "SELECT tl FROM TrackedLink tl " + "WHERE tl.chat.id = :chatId AND tl.url = :url",
                        TrackedLink.class)
                .setParameter("chatId", chatId)
                .setParameter("url", url)
                .getSingleResult();

        em.remove(link);
    }

    @Override
    public List<LinkResponse> getLinks(long chatId, int offset, int limit) {
        // JPQL запрос для загрузки TrackedLink с тегами и фильтрами
        String jpql =
                """
            SELECT DISTINCT tl FROM TrackedLink tl
            LEFT JOIN FETCH tl.tags t
            LEFT JOIN FETCH tl.filters f
            WHERE tl.chat.id = :chatId
        """;

        TypedQuery<TrackedLink> query = em.createQuery(jpql, TrackedLink.class);
        query.setParameter("chatId", chatId);
        query.setFirstResult(offset); // Начальная позиция
        query.setMaxResults(limit); // Количество записей

        List<TrackedLink> trackedLinks = query.getResultList();

        // Преобразование TrackedLink в LinkResponse
        return mapToLinkResponses(trackedLinks);
    }

    @Override
    @Transactional
    public void registerChat(long chatId) {
        if (em.find(TgChat.class, chatId) == null) {
            TgChat chat = new TgChat();
            chat.setId(chatId);
            em.persist(chat);
        }
    }

    @Override
    @Transactional
    public void deleteChat(long chatId) {
        TgChat chat = em.getReference(TgChat.class, chatId);
        em.remove(chat);
    }

    @Override
    public List<Long> getAllChatIds() {
        return em.createQuery("SELECT tc.id FROM TgChat tc", Long.class).getResultList();
    }

    @Override
    @Transactional
    public void addTags(long chatId, String url, List<String> tags) {
        TrackedLink link = em.createQuery(
                        "SELECT tl FROM TrackedLink tl WHERE tl.chat.id = :chatId AND tl.url = :url", TrackedLink.class)
                .setParameter("chatId", chatId)
                .setParameter("url", url)
                .getSingleResult();

        tags.forEach(tagName -> {
            Tag tag = new Tag();
            tag.setName(tagName);
            tag.setLink(link);
            link.getTags().add(tag);
        });
    }

    @Override
    @Transactional
    public void removeTag(long chatId, String url, String tagName) {
        em.createQuery("DELETE FROM Tag t WHERE t.link.chat.id = :chatId AND t.link.url = :url AND t.name = :tagName")
                .setParameter("chatId", chatId)
                .setParameter("url", url)
                .setParameter("tagName", tagName)
                .executeUpdate();
    }

    @Override
    public List<String> getTagsForLink(long chatId, String url) {
        return em.createQuery(
                        "SELECT t.name FROM Tag t WHERE t.link.chat.id = :chatId AND t.link.url = :url", String.class)
                .setParameter("chatId", chatId)
                .setParameter("url", url)
                .getResultList();
    }

    @Override
    public List<LinkResponse> getLinksByTag(long chatId, String tagName) {
        String jpql =
                """
        SELECT DISTINCT tl FROM TrackedLink tl
        JOIN tl.tags t
        WHERE tl.chat.id = :chatId AND t.name = :tagName
    """;

        TypedQuery<TrackedLink> query = em.createQuery(jpql, TrackedLink.class);
        query.setParameter("chatId", chatId);
        query.setParameter("tagName", tagName);

        List<TrackedLink> trackedLinks = query.getResultList();

        return mapToLinkResponses(trackedLinks);
    }

    private List<LinkResponse> mapToLinkResponses(List<TrackedLink> trackedLinks) {
        return trackedLinks.stream()
                .map(link -> new LinkResponse(
                        link.getId(),
                        link.getUrl(),
                        link.getTags() != null
                                ? link.getTags().stream()
                                        .map(Tag::getName)
                                        .distinct()
                                        .toList()
                                : Collections.emptyList(),
                        link.getFilters() != null
                                ? link.getFilters().stream()
                                        .map(Filter::getValue)
                                        .distinct()
                                        .toList()
                                : Collections.emptyList()))
                .toList();
    }
}

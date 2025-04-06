package backend.academy.scrapper.repository;

import backend.academy.model.LinkResponse;
import backend.academy.scrapper.repository.DTO.Filter;
import backend.academy.scrapper.repository.DTO.Tag;
import backend.academy.scrapper.repository.DTO.TgChat;
import backend.academy.scrapper.repository.DTO.TrackedLink;
import backend.academy.scrapper.repository.repos.FilterRepository;
import backend.academy.scrapper.repository.repos.TagRepository;
import backend.academy.scrapper.repository.repos.TgChatRepository;
import backend.academy.scrapper.repository.repos.TrackedLinkRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public class OrmLinkRepository implements LinkRepository {
    private final TgChatRepository tgChatRepository;
    private final TrackedLinkRepository trackedLinkRepository;
    private final TagRepository tagRepository;
    private final FilterRepository filterRepository;

    public OrmLinkRepository(TgChatRepository tgChatRepository, TrackedLinkRepository trackedLinkRepository, TagRepository tagRepository, FilterRepository filterRepository) {
        this.tgChatRepository = tgChatRepository;
        this.trackedLinkRepository = trackedLinkRepository;
        this.tagRepository = tagRepository;
        this.filterRepository = filterRepository;
    }

    @Override
    @Transactional
    public void addLink(long chatId, String url, List<String> tags, List<String> filters) {
        // Проверяем, существует ли ссылка
        if (trackedLinkRepository.findByChatIdAndUrl(chatId, url).isPresent()) {
            throw new IllegalArgumentException("Ссылка уже отслеживается.");
        }

        // Получаем или создаем чат
        TgChat chat = tgChatRepository.findById(chatId)
            .orElseGet(() -> {
                TgChat newChat = new TgChat();
                newChat.setId(chatId);
                return tgChatRepository.save(newChat);
            });

        // Создаем новую ссылку
        TrackedLink newLink = new TrackedLink();
        newLink.setUrl(url);
        newLink.setChat(chat);
        newLink.setLastChecked(LocalDateTime.now());

        // Добавляем теги
        tags.forEach(tagName -> {
            Tag tag = new Tag();
            tag.setName(tagName);
            newLink.getTags().add(tag);
        });

        // Добавляем фильтры
        filters.forEach(filterValue -> {
            Filter filter = new Filter();
            filter.setValue(filterValue);
            newLink.getFilters().add(filter);
        });

        trackedLinkRepository.save(newLink);
    }

    @Override
    @Transactional
    public void removeLink(long chatId, String url) {
        TrackedLink link = trackedLinkRepository.findByChatIdAndUrl(chatId, url)
            .orElseThrow(() -> new IllegalArgumentException("Ссылка не найдена."));
        trackedLinkRepository.delete(link);
    }

    @Override
    public List<LinkResponse> getLinks(long chatId, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        List<TrackedLink> trackedLinks = trackedLinkRepository.findByChatId(chatId, pageable);
        return mapToLinkResponses(trackedLinks);
    }

    @Override
    @Transactional
    public void registerChat(long chatId) {
        if (!tgChatRepository.existsById(chatId)) {
            TgChat chat = new TgChat();
            chat.setId(chatId);
            tgChatRepository.save(chat);
        }
    }

    @Override
    @Transactional
    public void deleteChat(long chatId) {
        tgChatRepository.deleteById(chatId);
    }

    @Override
    public List<Long> getAllChatIds() {
        return tgChatRepository.findAll().stream()
            .map(TgChat::getId)
            .toList();
    }

    @Override
    @Transactional
    public void addTags(long chatId, String url, List<String> tags) {
        TrackedLink link = trackedLinkRepository.findByChatIdAndUrl(chatId, url)
            .orElseThrow(() -> new IllegalArgumentException("Ссылка не найдена."));

        tags.forEach(tagName -> {
            Tag tag = new Tag();
            tag.setName(tagName);
            tag.setLink(link);
            link.getTags().add(tag);
        });

        trackedLinkRepository.save(link);
    }

    @Override
    @Transactional
    public void removeTag(long chatId, String url, String tagName) {
        TrackedLink link = trackedLinkRepository.findByChatIdAndUrl(chatId, url)
            .orElseThrow(() -> new IllegalArgumentException("Ссылка не найдена."));

        link.getTags().removeIf(tag -> tag.getName().equals(tagName));
        trackedLinkRepository.save(link);
    }

    @Override
    public List<String> getTagsForLink(long chatId, String url) {
        TrackedLink link = trackedLinkRepository.findByChatIdAndUrl(chatId, url)
            .orElseThrow(() -> new IllegalArgumentException("Ссылка не найдена."));
        return link.getTags().stream()
            .map(Tag::getName)
            .toList();
    }

    @Override
    public List<LinkResponse> getLinksByTag(long chatId, String tagName) {
        List<TrackedLink> trackedLinks = trackedLinkRepository.findByChatIdAndTagName(chatId, tagName);
        return mapToLinkResponses(trackedLinks);
    }

    private List<LinkResponse> mapToLinkResponses(List<TrackedLink> trackedLinks) {
        return trackedLinks.stream()
            .map(link -> new LinkResponse(
                link.getId(),
                link.getUrl(),
                link.getTags().stream()
                    .map(Tag::getName)
                    .distinct()
                    .toList(),
                link.getFilters().stream()
                    .map(Filter::getValue)
                    .distinct()
                    .toList()))
            .toList();
    }
}

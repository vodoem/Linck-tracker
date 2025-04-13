package backend.academy.scrapper.service;

import backend.academy.model.AddLinkRequest;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import backend.academy.model.RemoveLinkRequest;
import backend.academy.model.RemoveTagRequest;
import backend.academy.scrapper.exceptionhandler.ResourceNotFoundException;
import backend.academy.scrapper.repository.LinkRepository;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LinkService {

    private static final Pattern URL_PATTERN =
            Pattern.compile("^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?$", Pattern.CASE_INSENSITIVE);

    private final LinkRepository linkRepository;

    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Transactional
    public void addLink(long chatId, AddLinkRequest request) {
        validateUrl(request.link());

        if (linkRepository.existsByChatIdAndUrl(chatId, request.link())) {
            throw new IllegalArgumentException("Ссылка уже отслеживается.");
        }

        linkRepository.addLink(chatId, request.link(), request.tags(), request.filters());
    }

    @Transactional
    public void removeLink(long chatId, RemoveLinkRequest request) {
        if (!linkRepository.existsByChatIdAndUrl(chatId, request.link())) {
            throw new ResourceNotFoundException("Ссылка не найдена.");
        }

        linkRepository.removeLink(chatId, request.link());
    }

    public ListLinksResponse getLinks(long chatId, int offset, int limit) {
        List<LinkResponse> links = linkRepository.getLinks(chatId, offset, limit);
        return new ListLinksResponse(links, links.size());
    }

    @Transactional
    public void registerChat(long chatId) {
        if (chatId <= 0) {
            throw new IllegalArgumentException("Некорректный ID чата.");
        }

        linkRepository.registerChat(chatId);
    }

    @Transactional
    public void deleteChat(long chatId) {
        if (!linkRepository.getAllChatIds().contains(chatId)) {
            throw new ResourceNotFoundException("Чат с ID " + chatId + " не существует.");
        }

        linkRepository.deleteChat(chatId);
    }

    @Transactional
    public void addTags(long chatId, String url, List<String> tags) {
        if (!linkRepository.existsByChatIdAndUrl(chatId, url)) {
            throw new ResourceNotFoundException("Ссылка не найдена.");
        }

        linkRepository.addTags(chatId, url, tags);
    }

    @Transactional
    public void removeTag(long chatId, RemoveTagRequest request) {
        if (!linkRepository.existsByChatIdAndUrl(chatId, request.url())) {
            throw new ResourceNotFoundException("Ссылка не найдена.");
        }

        linkRepository.removeTag(chatId, request.url(), request.tagName());
    }

    public List<String> getTagsForLink(long chatId, String url) {
        if (!linkRepository.existsByChatIdAndUrl(chatId, url)) {
            throw new ResourceNotFoundException("Ссылка не найдена.");
        }

        return linkRepository.getTagsForLink(chatId, url);
    }

    public ListLinksResponse getLinksByTag(long chatId, String tagName) {
        List<LinkResponse> links = linkRepository.getLinksByTag(chatId, tagName);
        return new ListLinksResponse(links, links.size());
    }

    private void validateUrl(String url) {
        if (url == null || !URL_PATTERN.matcher(url).matches()) {
            throw new IllegalArgumentException("Некорректный URL.");
        }
    }
}

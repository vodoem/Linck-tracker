package backend.academy.scrapper.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.repository.DTO.Filter;
import backend.academy.scrapper.repository.DTO.Tag;
import backend.academy.scrapper.repository.DTO.TgChat;
import backend.academy.scrapper.repository.DTO.TrackedLink;
import backend.academy.scrapper.repository.repos.TgChatRepository;
import backend.academy.scrapper.repository.repos.TrackedLinkRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class OrmLinkRepositoryUnitTest {
    private final TgChatRepository tgChatRepository = org.mockito.Mockito.mock(TgChatRepository.class);
    private final TrackedLinkRepository trackedLinkRepository = org.mockito.Mockito.mock(TrackedLinkRepository.class);
    private final OrmLinkRepository repository = new OrmLinkRepository(tgChatRepository, trackedLinkRepository);

    @Test
    void addLinkBindsTagsAndFiltersToSavedLink() {
        TgChat chat = new TgChat();
        chat.setId(42L);
        when(tgChatRepository.findById(42L)).thenReturn(Optional.of(chat));
        when(trackedLinkRepository.save(any(TrackedLink.class))).thenAnswer(invocation -> invocation.getArgument(0));

        repository.addLink(42L, "https://github.com/owner/repo", List.of("work"), List.of("user:owner"));

        ArgumentCaptor<TrackedLink> captor = ArgumentCaptor.forClass(TrackedLink.class);
        verify(trackedLinkRepository).save(captor.capture());
        TrackedLink savedLink = captor.getValue();

        assertThat(savedLink.getTags()).singleElement().extracting(Tag::getLink).isSameAs(savedLink);
        assertThat(savedLink.getFilters())
                .singleElement()
                .extracting(Filter::getLink)
                .isSameAs(savedLink);
    }

    @Test
    void addLinkAcceptsNullTagsAndFilters() {
        TgChat chat = new TgChat();
        chat.setId(42L);
        when(tgChatRepository.findById(42L)).thenReturn(Optional.of(chat));
        when(trackedLinkRepository.save(any(TrackedLink.class))).thenAnswer(invocation -> invocation.getArgument(0));

        repository.addLink(42L, "https://github.com/owner/repo", null, null);

        ArgumentCaptor<TrackedLink> captor = ArgumentCaptor.forClass(TrackedLink.class);
        verify(trackedLinkRepository).save(captor.capture());
        TrackedLink savedLink = captor.getValue();

        assertThat(savedLink.getTags()).isEmpty();
        assertThat(savedLink.getFilters()).isEmpty();
    }
}

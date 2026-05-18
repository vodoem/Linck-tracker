package backend.academy.scrapper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import backend.academy.model.LinkResponse;
import backend.academy.scrapper.repository.LinkRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class LinkCheckerSchedulerTest {
    private final LinkCheckerScheduler scheduler =
            new LinkCheckerScheduler(mock(LinkRepository.class), mock(CommunicationService.class), List.of());

    @Test
    void shouldSplitRemainderBatchWithoutInvalidSubListRange() {
        List<List<LinkResponse>> batches = scheduler.splitIntoSubBatches(links(5), 4);

        assertThat(batches).hasSize(3);
        assertThat(batches).extracting(List::size).containsExactly(2, 2, 1);
    }

    @Test
    void shouldNotCreateMoreBatchesThanLinksWhenThreadCountIsGreaterThanLinkCount() {
        List<List<LinkResponse>> batches = scheduler.splitIntoSubBatches(links(3), 10);

        assertThat(batches).hasSize(3);
        assertThat(batches).extracting(List::size).containsExactly(1, 1, 1);
    }

    @Test
    void shouldFallbackToSingleThreadWhenConfiguredThreadCountIsInvalid() {
        List<List<LinkResponse>> batches = scheduler.splitIntoSubBatches(links(3), 0);

        assertThat(batches).hasSize(1);
        assertThat(batches.getFirst()).hasSize(3);
    }

    private List<LinkResponse> links(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index ->
                        new LinkResponse(index, "https://github.com/example/repo/" + index, List.of(), List.of()))
                .toList();
    }
}

package com.example.dopamenu.service;

import com.example.dopamenu.domain.MenuEvent;
import com.example.dopamenu.repository.MenuEventRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class MenuEventServiceTest {

    private final MenuEventRepository repository = mock(MenuEventRepository.class);
    private final MenuEventService service = new MenuEventService(repository);

    private MenuEvent saved() {
        ArgumentCaptor<MenuEvent> captor = ArgumentCaptor.forClass(MenuEvent.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void everyKindIsWrittenWithItsStableCode() {
        UUID orderId = UUID.randomUUID();

        service.record(MenuEvent.Kind.ORDER_STARTED, "user-1", "token", orderId,
                "https://example.com/thread");

        MenuEvent event = saved();
        assertThat(event.getKind()).isEqualTo(MenuEvent.Kind.ORDER_STARTED);
        assertThat(event.getKind().code()).isEqualTo("order_started");
        assertThat(event.getToken()).isEqualTo("token");
        assertThat(event.getOwnerSub()).isEqualTo("user-1");
        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getOccurredAt()).isNotNull();
    }

    @Test
    void shareViewIsSkippedForKnownBots() {
        service.recordShareView("token",
                "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
                null);

        verify(repository, never()).save(any());
    }

    @Test
    void shareViewIsSkippedWithoutAToken() {
        service.recordShareView("  ", "Mozilla/5.0", null);

        verify(repository, never()).save(any());
    }

    @Test
    void shareViewIsRecordedForAnOrdinaryBrowser() {
        service.recordShareView("token",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) Safari/604.1",
                "https://example.com/thread");

        MenuEvent event = saved();
        assertThat(event.getKind()).isEqualTo(MenuEvent.Kind.SHARE_VIEW);
        assertThat(event.getReferrer()).isEqualTo("https://example.com/thread");
        assertThat(event.getOwnerSub()).isNull();
    }

    @Test
    void aVeryLongReferrerIsTruncatedToTheColumnWidth() {
        service.record(MenuEvent.Kind.SHARE_VIEW, null, "token", null, "a".repeat(600));

        assertThat(saved().getReferrer()).hasSize(512);
    }
}

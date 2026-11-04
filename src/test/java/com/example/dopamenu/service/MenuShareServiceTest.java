package com.example.dopamenu.service;

import com.example.dopamenu.domain.MenuEvent;
import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.domain.MenuShare;
import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.DopamineLevel;
import com.example.dopamenu.repository.MenuShareRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MenuShareServiceTest {

    private final MenuShareRepository repository = mock(MenuShareRepository.class);
    private final MenuItemService menuItems = mock(MenuItemService.class);
    private final MenuEventService events = mock(MenuEventService.class);
    private final MenuShareService service = new MenuShareService(repository, menuItems, events);

    private static void saveReturnsArgument(MenuShareRepository repository) {
        when(repository.save(any(MenuShare.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createMintsATwentyTwoCharacterUrlSafeToken() {
        saveReturnsArgument(repository);
        when(repository.findFirstByOwnerSubAndRevokedAtIsNullOrderByCreatedAtDesc("user-1"))
                .thenReturn(Optional.empty());

        MenuShare share = service.create("user-1", "Micah");

        assertThat(share.getToken()).hasSize(22).matches("[A-Za-z0-9_-]{22}");
        assertThat(share.getDisplayName()).isEqualTo("Micah");
        assertThat(share.isActive()).isTrue();
        verify(repository).save(share);
    }

    @Test
    void createRejectsASecondActiveLink() {
        MenuShare active = new MenuShare("user-1", "abc", "Micah");
        when(repository.findFirstByOwnerSubAndRevokedAtIsNullOrderByCreatedAtDesc("user-1"))
                .thenReturn(Optional.of(active));

        assertThatThrownBy(() -> service.create("user-1", "Micah"))
                .isInstanceOf(MenuShareService.ActiveShareException.class)
                .satisfies(error -> assertThat(
                        ((MenuShareService.ActiveShareException) error).getShare())
                        .isSameAs(active));
        verify(repository, never()).save(any());
    }

    @Test
    void createRequiresAnAuthorName() {
        assertThatThrownBy(() -> service.create("user-1", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("author name");
    }

    @Test
    void rotateRevokesTheOldLinkBeforeCreatingTheNewOne() {
        MenuShare active = new MenuShare("user-1", "old-token", "Micah");
        when(repository.findFirstByOwnerSubAndRevokedAtIsNullOrderByCreatedAtDesc("user-1"))
                .thenReturn(Optional.of(active));
        saveReturnsArgument(repository);

        MenuShare rotated = service.rotate("user-1", "Micah");

        assertThat(active.isActive()).isFalse();
        assertThat(rotated.isActive()).isTrue();
        assertThat(rotated.getToken()).isNotEqualTo("old-token");
        verify(repository).saveAndFlush(active);
    }

    @Test
    void revokeOnlyTouchesTheOwnersOwnShare() {
        MenuShare share = new MenuShare("user-1", "token", "Micah");
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(share));

        service.revoke("someone-else", id);

        assertThat(share.isActive()).isTrue();
    }

    @Test
    void liveIgnoresBlankTokensWithoutHittingTheDatabase() {
        assertThat(service.live("  ")).isEmpty();
        assertThat(service.live(null)).isEmpty();
        verify(repository, never()).findByTokenAndRevokedAtIsNull(any());
    }

    @Test
    void copyAllRecordsOneMenuCopiedEventWhenRowsAreCreated() {
        MenuShare share = new MenuShare("sharer", "token", "Micah");
        MenuItem item = new MenuItem("sharer", Course.ENTREE, "A long walk", null,
                45, DopamineLevel.BRIGHT, false);
        MenuItem created = new MenuItem("visitor", Course.ENTREE, "A long walk", null,
                45, DopamineLevel.BRIGHT, false, "token");
        when(repository.findByTokenAndRevokedAtIsNull("token")).thenReturn(Optional.of(share));
        when(menuItems.menuFor("sharer")).thenReturn(List.of(item));
        when(menuItems.copyFrom("visitor", List.of(item), "token")).thenReturn(List.of(created));

        assertThat(service.copyAll("visitor", "token")).containsExactly(created);
        verify(events).record(MenuEvent.Kind.MENU_COPIED, "visitor", "token", null, null);
    }

    @Test
    void copyAllCreatesNoEventWhenEverythingWasAlreadyOwned() {
        MenuShare share = new MenuShare("sharer", "token", "Micah");
        MenuItem item = new MenuItem("sharer", Course.ENTREE, "A long walk", null,
                45, DopamineLevel.BRIGHT, false);
        when(repository.findByTokenAndRevokedAtIsNull("token")).thenReturn(Optional.of(share));
        when(menuItems.menuFor("sharer")).thenReturn(List.of(item));
        when(menuItems.copyFrom("visitor", List.of(item), "token")).thenReturn(List.of());

        assertThat(service.copyAll("visitor", "token")).isEmpty();
        verify(events, never()).record(any(), any(), any(), any(), any());
    }

    @Test
    void copyOneCopiesOnlyTheRequestedItem() {
        MenuShare share = new MenuShare("sharer", "token", "Micah");
        UUID wantedId = UUID.randomUUID();
        MenuItem wanted = mock(MenuItem.class);
        MenuItem other = mock(MenuItem.class);
        MenuItem created = mock(MenuItem.class);
        when(wanted.getId()).thenReturn(wantedId);
        when(other.getId()).thenReturn(UUID.randomUUID());
        when(repository.findByTokenAndRevokedAtIsNull("token")).thenReturn(Optional.of(share));
        when(menuItems.menuFor("sharer")).thenReturn(List.of(wanted, other));
        when(menuItems.copyFrom("visitor", List.of(wanted), "token"))
                .thenReturn(List.of(created));

        assertThat(service.copyOne("visitor", "token", wantedId)).contains(created);
        verify(menuItems).copyFrom("visitor", List.of(wanted), "token");
    }

    @Test
    void copyOneReturnsTheExistingDishWhenTheVisitorAlreadyOwnedIt() {
        MenuShare share = new MenuShare("sharer", "token", "Micah");
        UUID wantedId = UUID.randomUUID();
        MenuItem source = mock(MenuItem.class);
        MenuItem existing = mock(MenuItem.class);
        when(source.getId()).thenReturn(wantedId);
        when(source.getCourse()).thenReturn(Course.ENTREE);
        when(source.getTitle()).thenReturn("A long walk");
        when(existing.getCourse()).thenReturn(Course.ENTREE);
        when(existing.getTitle()).thenReturn("a long walk");
        when(repository.findByTokenAndRevokedAtIsNull("token")).thenReturn(Optional.of(share));
        when(menuItems.menuFor("sharer")).thenReturn(List.of(source));
        when(menuItems.copyFrom("visitor", List.of(source), "token")).thenReturn(List.of());
        when(menuItems.menuFor("visitor")).thenReturn(List.of(existing));

        assertThat(service.copyOne("visitor", "token", wantedId)).contains(existing);
    }

    @Test
    void anIntentIsSingleUse() {
        MockHttpSession session = new MockHttpSession();
        ShareIntent intent = ShareIntent.order("token", UUID.randomUUID());
        session.setAttribute(MenuShareService.INTENT_KEY, intent);

        assertThat(service.consumeIntent(session)).contains(intent);
        assertThat(service.consumeIntent(session)).isEmpty();
        assertThat(session.getAttribute(MenuShareService.INTENT_KEY)).isNull();
    }

    @Test
    void anExpiredIntentIsDropped() {
        MockHttpSession session = new MockHttpSession();
        ShareIntent expired = new ShareIntent("token", UUID.randomUUID(),
                ShareIntent.Action.ORDER, Instant.now().minusSeconds(5));
        session.setAttribute(MenuShareService.INTENT_KEY, expired);

        assertThat(service.consumeIntent(session)).isEmpty();
        assertThat(session.getAttribute(MenuShareService.INTENT_KEY)).isNull();
    }
}

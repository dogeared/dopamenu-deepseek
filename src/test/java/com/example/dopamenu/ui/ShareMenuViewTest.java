package com.example.dopamenu.ui;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.DopamineLevel;
import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.domain.MenuShare;
import com.example.dopamenu.service.CurrentUser;
import com.example.dopamenu.service.MenuEventService;
import com.example.dopamenu.service.MenuItemService;
import com.example.dopamenu.service.MenuShareService;
import com.example.dopamenu.ui.component.CourseSection;
import com.example.dopamenu.ui.component.MenuItemCard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.example.dopamenu.ui.UiTestSupport.findAll;
import static com.example.dopamenu.ui.UiTestSupport.hasText;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShareMenuViewTest {

    private final MenuShareService shareService = mock(MenuShareService.class);
    private final MenuItemService menuItemService = mock(MenuItemService.class);
    private final MenuEventService events = mock(MenuEventService.class);
    private final CurrentUser currentUser = mock(CurrentUser.class);

    @BeforeEach
    void setUp() {
        UiTestSupport.installUi();
    }

    @AfterEach
    void tearDown() {
        UiTestSupport.removeUi();
    }

    private ShareMenuView view() {
        return new ShareMenuView(shareService, menuItemService, events, currentUser);
    }

    private MenuShare share() {
        return new MenuShare("sharer", "token", "Micah");
    }

    private MenuItem item(Course course, String title) {
        return new MenuItem("sharer", course, title, "Description of " + title,
                15, DopamineLevel.STEADY, false);
    }

    @Test
    void rendersALiveSharedMenuAndRecordsTheView() {
        when(shareService.live("token")).thenReturn(Optional.of(share()));
        when(menuItemService.menuFor("sharer")).thenReturn(List.of(
                item(Course.APPETIZER, "Water"),
                item(Course.ENTREE, "A long walk")));
        ShareMenuView view = view();

        view.render("token");

        assertThat(hasText(view, "Micah’s menu")).isTrue();
        assertThat(hasText(view, "Copy this menu")).isTrue();
        assertThat(findAll(view, CourseSection.class)).hasSize(4);
        assertThat(findAll(view, MenuItemCard.class)).hasSize(2);
        assertThat(hasText(view, "Add something")).isFalse();
        verify(events).recordShareView(eq("token"), any(), any());
    }

    @Test
    void aDeadTokenRendersTheOneDeadPageWithoutRecording() {
        when(shareService.live("dead")).thenReturn(Optional.empty());
        ShareMenuView view = view();

        view.render("dead");

        assertThat(hasText(view, "This link is no longer active.")).isTrue();
        assertThat(hasText(view, "Copy this menu")).isFalse();
        verify(events, never()).recordShareView(any(), any(), any());
    }

    @Test
    void aFailingEventWriteStillRendersTheMenu() {
        when(shareService.live("token")).thenReturn(Optional.of(share()));
        when(menuItemService.menuFor("sharer"))
                .thenReturn(List.of(item(Course.ENTREE, "A long walk")));
        doThrow(new IllegalStateException("event store is down"))
                .when(events).recordShareView(eq("token"), any(), any());
        ShareMenuView view = view();

        view.render("token");

        assertThat(hasText(view, "Micah’s menu")).isTrue();
        assertThat(findAll(view, MenuItemCard.class)).hasSize(1);
    }

    @Test
    void theOwnerPreviewSeesTheirOwnMenuWithoutACopyButton() {
        when(currentUser.isAuthenticated()).thenReturn(true);
        when(currentUser.id()).thenReturn("sharer");
        when(shareService.live("token")).thenReturn(Optional.of(share()));
        when(menuItemService.menuFor("sharer")).thenReturn(List.of());
        ShareMenuView view = view();

        view.render("token");

        assertThat(hasText(view, "already your menu")).isTrue();
        assertThat(hasText(view, "Copy this menu")).isFalse();
        verify(events, never()).recordShareView(any(), any(), any());
    }
}

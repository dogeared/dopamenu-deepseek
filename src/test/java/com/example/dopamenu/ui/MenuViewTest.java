package com.example.dopamenu.ui;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.DopamineLevel;
import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.domain.MenuOrder;
import com.example.dopamenu.domain.MenuShare;
import com.example.dopamenu.service.CurrentUser;
import com.example.dopamenu.service.MenuItemService;
import com.example.dopamenu.service.MenuOrderService;
import com.example.dopamenu.service.MenuShareService;
import com.example.dopamenu.service.OrderStats;
import com.example.dopamenu.service.ShareIntent;
import com.example.dopamenu.ui.component.CourseSection;
import com.example.dopamenu.ui.component.MenuItemCard;
import com.example.dopamenu.ui.component.OrderDialog;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.example.dopamenu.ui.UiTestSupport.button;
import static com.example.dopamenu.ui.UiTestSupport.click;
import static com.example.dopamenu.ui.UiTestSupport.findAll;
import static com.example.dopamenu.ui.UiTestSupport.hasText;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MenuViewTest {

    private final MenuItemService menuItemService = mock(MenuItemService.class);
    private final MenuOrderService orderService = mock(MenuOrderService.class);
    private final MenuShareService shareService = mock(MenuShareService.class);
    private final CurrentUser currentUser = mock(CurrentUser.class);

    @BeforeEach
    void setUp() {
        UiTestSupport.installUi();
        when(currentUser.id()).thenReturn("user-1");
    }

    @AfterEach
    void tearDown() {
        UiTestSupport.removeUi();
    }

    private MenuItem item(Course course, String title) {
        return new MenuItem("user-1", course, title, "Description of " + title,
                15, DopamineLevel.STEADY, course.caution());
    }

    private OrderStats.Snapshot stats() {
        return new OrderStats.Snapshot(3, 90, 75, Course.ENTREE, 1.5);
    }

    @Test
    void rendersTheHeroStatsAndEveryCourse() {
        List<MenuItem> items = List.of(
                item(Course.APPETIZER, "Water"),
                item(Course.ENTREE, "A long walk"),
                item(Course.SIDE, "Podcast"),
                item(Course.DESSERT, "One video"));
        when(menuItemService.ensureSeeded("user-1")).thenReturn(items);
        when(orderService.stats("user-1", 7)).thenReturn(stats());
        when(orderService.activeOrder("user-1")).thenReturn(Optional.empty());

        MenuView view = new MenuView(menuItemService, orderService, shareService,
                currentUser);

        assertThat(hasText(view, "The dopa menu")).isTrue();
        assertThat(hasText(view, "Feed the need, not the scroll.")).isTrue();
        assertThat(hasText(view, "3")).isTrue();
        assertThat(hasText(view, "orders logged")).isTrue();
        assertThat(hasText(view, "1 hr 30 min")).isTrue();
        assertThat(hasText(view, "4")).isTrue();
        assertThat(hasText(view, "dishes on your menu")).isTrue();
        assertThat(findAll(view, CourseSection.class)).hasSize(4);
        assertThat(findAll(view, MenuItemCard.class)).hasSize(4);
        assertThat(findAll(view, SiteFooter.class)).hasSize(1);
        assertThat(findAll(view, Anchor.class))
                .anyMatch(anchor -> SiteFooter.GITHUB_URL.equals(anchor.getHref()));
    }

    @Test
    void anEmptyMenuStillShowsAllFourCourses() {
        when(menuItemService.ensureSeeded("user-1")).thenReturn(List.of());
        when(orderService.stats("user-1", 7))
                .thenReturn(new OrderStats.Snapshot(0, 0, 0, null, null));
        when(orderService.activeOrder("user-1")).thenReturn(Optional.empty());

        MenuView view = new MenuView(menuItemService, orderService, shareService,
                currentUser);

        assertThat(findAll(view, CourseSection.class)).hasSize(4);
        assertThat(findAll(view, MenuItemCard.class)).isEmpty();
        assertThat(hasText(view, "Nothing on this course yet.")).isTrue();
    }

    @Test
    void aRunningOrderIsShownInABanner() {
        MenuItem item = item(Course.ENTREE, "A long walk");
        MenuOrder running = new MenuOrder("user-1", item, 3);
        when(menuItemService.ensureSeeded("user-1")).thenReturn(List.of(item));
        when(orderService.stats("user-1", 7)).thenReturn(stats());
        when(orderService.activeOrder("user-1")).thenReturn(Optional.of(running));

        MenuView view = new MenuView(menuItemService, orderService, shareService,
                currentUser);

        assertThat(hasText(view, "On the table")).isTrue();
        assertThat(hasText(view, "A long walk")).isTrue();
        assertThat(hasText(view, "Started")).isTrue();
        assertThat(hasText(view, "Resume")).isTrue();
    }

    @Test
    void orderingADishOpensTheOrderDialog() {
        MenuItem item = item(Course.APPETIZER, "Water");
        when(menuItemService.ensureSeeded("user-1")).thenReturn(List.of(item));
        when(orderService.stats("user-1", 7)).thenReturn(stats());
        when(orderService.activeOrder("user-1")).thenReturn(Optional.empty());
        MenuView view = new MenuView(menuItemService, orderService, shareService,
                currentUser);

        click(button(view, "Order"));

        assertThat(findAll(UI.getCurrent(), Dialog.class))
                .hasSize(1)
                .first()
                .isInstanceOf(OrderDialog.class);
    }

    @Test
    void resumingTheRunningOrderOpensTheOrderDialog() {
        MenuItem item = item(Course.ENTREE, "A long walk");
        MenuOrder running = new MenuOrder("user-1", item, 3);
        when(menuItemService.ensureSeeded("user-1")).thenReturn(List.of(item));
        when(orderService.stats("user-1", 7)).thenReturn(stats());
        when(orderService.activeOrder("user-1")).thenReturn(Optional.of(running));

        MenuView view = new MenuView(menuItemService, orderService, shareService,
                currentUser);

        click(button(view, "Resume"));

        assertThat(findAll(UI.getCurrent(), Dialog.class)).hasSize(1);
    }

    @Test
    void noIntentLeavesTheMenuUntouched() {
        MenuItem item = item(Course.APPETIZER, "Water");
        when(menuItemService.ensureSeeded("user-1")).thenReturn(List.of(item));
        when(orderService.stats("user-1", 7)).thenReturn(stats());
        when(orderService.activeOrder("user-1")).thenReturn(Optional.empty());
        when(shareService.consumeIntent()).thenReturn(Optional.empty());
        MenuView view = new MenuView(menuItemService, orderService, shareService,
                currentUser);

        view.afterNavigation(null);

        verify(shareService, never()).live(any());
        verify(shareService, never()).copyOne(any(), any(), any());
        assertThat(findAll(UI.getCurrent(), Dialog.class)).isEmpty();
    }

    @Test
    void anOrderIntentCopiesExactlyOneItemAndOpensTheBeforeDialog() {
        MenuItem item = item(Course.ENTREE, "A long walk");
        MenuItem copied = item(Course.ENTREE, "A long walk");
        UUID sourceId = UUID.randomUUID();
        when(menuItemService.ensureSeeded("user-1")).thenReturn(List.of(item));
        when(orderService.stats("user-1", 7)).thenReturn(stats());
        when(orderService.activeOrder("user-1")).thenReturn(Optional.empty());
        when(shareService.consumeIntent())
                .thenReturn(Optional.of(ShareIntent.order("token", sourceId)));
        when(shareService.live("token"))
                .thenReturn(Optional.of(new MenuShare("sharer", "token", "Micah")));
        when(shareService.copyOne("user-1", "token", sourceId))
                .thenReturn(Optional.of(copied));
        MenuView view = new MenuView(menuItemService, orderService, shareService,
                currentUser);

        view.afterNavigation(null);

        verify(shareService).copyOne("user-1", "token", sourceId);
        assertThat(findAll(UI.getCurrent(), Dialog.class))
                .hasSize(1)
                .first()
                .isInstanceOf(OrderDialog.class);
    }

    @Test
    void aCopyIntentCopiesTheWholeMenu() {
        MenuItem item = item(Course.APPETIZER, "Water");
        when(menuItemService.ensureSeeded("user-1")).thenReturn(List.of(item));
        when(orderService.stats("user-1", 7)).thenReturn(stats());
        when(orderService.activeOrder("user-1")).thenReturn(Optional.empty());
        when(shareService.consumeIntent())
                .thenReturn(Optional.of(ShareIntent.copy("token")));
        when(shareService.live("token"))
                .thenReturn(Optional.of(new MenuShare("sharer", "token", "Micah")));
        when(shareService.copyAll("user-1", "token")).thenReturn(List.of(item));
        MenuView view = new MenuView(menuItemService, orderService, shareService,
                currentUser);

        view.afterNavigation(null);

        verify(shareService).copyAll("user-1", "token");
    }

    @Test
    void aDeadTokenDropsTheIntentWithANotice() {
        MenuItem item = item(Course.APPETIZER, "Water");
        when(menuItemService.ensureSeeded("user-1")).thenReturn(List.of(item));
        when(orderService.stats("user-1", 7)).thenReturn(stats());
        when(orderService.activeOrder("user-1")).thenReturn(Optional.empty());
        when(shareService.consumeIntent())
                .thenReturn(Optional.of(ShareIntent.order("dead", UUID.randomUUID())));
        when(shareService.live("dead")).thenReturn(Optional.empty());
        MenuView view = spy(new MenuView(menuItemService, orderService, shareService,
                currentUser));

        view.afterNavigation(null);

        verify(shareService, never()).copyOne(any(), any(), any());
        verify(view).notice("That link is no longer active.");
    }
}

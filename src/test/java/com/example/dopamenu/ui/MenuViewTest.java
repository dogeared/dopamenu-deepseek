package com.example.dopamenu.ui;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.DopamineLevel;
import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.domain.MenuOrder;
import com.example.dopamenu.service.CurrentUser;
import com.example.dopamenu.service.MenuItemService;
import com.example.dopamenu.service.MenuOrderService;
import com.example.dopamenu.service.OrderStats;
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

import static com.example.dopamenu.ui.UiTestSupport.button;
import static com.example.dopamenu.ui.UiTestSupport.click;
import static com.example.dopamenu.ui.UiTestSupport.findAll;
import static com.example.dopamenu.ui.UiTestSupport.hasText;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MenuViewTest {

    private final MenuItemService menuItemService = mock(MenuItemService.class);
    private final MenuOrderService orderService = mock(MenuOrderService.class);
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

        MenuView view = new MenuView(menuItemService, orderService, currentUser);

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

        MenuView view = new MenuView(menuItemService, orderService, currentUser);

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

        MenuView view = new MenuView(menuItemService, orderService, currentUser);

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
        MenuView view = new MenuView(menuItemService, orderService, currentUser);

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

        MenuView view = new MenuView(menuItemService, orderService, currentUser);

        click(button(view, "Resume"));

        assertThat(findAll(UI.getCurrent(), Dialog.class)).hasSize(1);
    }
}

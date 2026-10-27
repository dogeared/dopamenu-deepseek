package com.example.dopamenu.ui;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.DopamineLevel;
import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.domain.MenuOrder;
import com.example.dopamenu.service.CurrentUser;
import com.example.dopamenu.service.MenuOrderService;
import com.example.dopamenu.service.OrderStats;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.example.dopamenu.ui.UiTestSupport.button;
import static com.example.dopamenu.ui.UiTestSupport.click;
import static com.example.dopamenu.ui.UiTestSupport.findAll;
import static com.example.dopamenu.ui.UiTestSupport.hasText;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HistoryViewTest {

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

    private MenuOrder order(Course course, String title, int before, int after,
            String note) {
        MenuItem item = new MenuItem("user-1", course, title, null, 45,
                DopamineLevel.STEADY, false);
        MenuOrder order = new MenuOrder("user-1", item, before);
        order.complete(after, note);
        return order;
    }

    private OrderStats.Snapshot stats() {
        return new OrderStats.Snapshot(2, 70, 67, Course.ENTREE, 1.5);
    }

    @Test
    void emptyHistoryExplainsWhereOrdersWillAppear() {
        when(orderService.stats("user-1", 7))
                .thenReturn(new OrderStats.Snapshot(0, 0, 0, null, null));
        when(orderService.recentOrders("user-1")).thenReturn(List.of());

        HistoryView view = new HistoryView(orderService, currentUser);

        assertThat(hasText(view, "What you actually did.")).isTrue();
        assertThat(hasText(view, "Nothing on the tab yet.")).isTrue();
        assertThat(hasText(view, "Order something from the menu")).isTrue();
        assertThat(hasText(view, "0 entries")).isTrue();
        assertThat(hasText(view, "—")).isTrue();
    }

    @Test
    void rendersTheWeeklyStats() {
        when(orderService.stats("user-1", 7)).thenReturn(stats());
        when(orderService.recentOrders("user-1")).thenReturn(List.of());

        HistoryView view = new HistoryView(orderService, currentUser);

        assertThat(hasText(view, "Finished")).isTrue();
        assertThat(hasText(view, "2")).isTrue();
        assertThat(hasText(view, "1 hr 10 min")).isTrue();
        assertThat(hasText(view, "67%")).isTrue();
        assertThat(hasText(view, "+1.5")).isTrue();
    }

    @Test
    void rendersCompletedAbandonedAndRunningOrders() {
        MenuOrder completed = order(Course.ENTREE, "A long walk", 2, 4, "Worth it");
        MenuOrder abandoned = order(Course.SIDE, "Podcast", 3, 3, "  ");
        abandoned.abandon();
        MenuItem item = new MenuItem("user-1", Course.APPETIZER, "Water", null,
                2, DopamineLevel.GENTLE, false);
        MenuOrder active = new MenuOrder("user-1", item, 3);
        when(orderService.stats("user-1", 7)).thenReturn(stats());
        when(orderService.recentOrders("user-1"))
                .thenReturn(List.of(completed, abandoned, active));

        HistoryView view = new HistoryView(orderService, currentUser);

        assertThat(hasText(view, "3 entries")).isTrue();
        assertThat(hasText(view, "Finished")).isTrue();
        assertThat(hasText(view, "Let go")).isTrue();
        assertThat(hasText(view, "Running")).isTrue();
        assertThat(hasText(view, "Worth it")).isTrue();
        assertThat(hasText(view, "Resume")).isTrue();
        assertThat(findAll(view, Span.class))
                .filteredOn(span -> span.getClassNames().contains("mood-delta"))
                .extracting(Span::getText)
                .containsExactly("+2");
    }

    @Test
    void aSingleEntryUsesTheSingularLabel() {
        MenuOrder completed = order(Course.ENTREE, "A long walk", 3, 3, null);
        when(orderService.stats("user-1", 7)).thenReturn(stats());
        when(orderService.recentOrders("user-1")).thenReturn(List.of(completed));

        HistoryView view = new HistoryView(orderService, currentUser);

        assertThat(hasText(view, "1 entry")).isTrue();
        assertThat(findAll(view, Span.class))
                .filteredOn(span -> span.getClassNames().contains("mood-delta"))
                .isEmpty();
    }

    @Test
    void resumingAnActiveOrderOpensTheOrderDialog() {
        MenuItem item = new MenuItem("user-1", Course.APPETIZER, "Water", null,
                2, DopamineLevel.GENTLE, false);
        MenuOrder active = new MenuOrder("user-1", item, 3);
        when(orderService.stats("user-1", 7)).thenReturn(stats());
        when(orderService.recentOrders("user-1")).thenReturn(List.of(active));
        HistoryView view = new HistoryView(orderService, currentUser);

        click(button(view, "Resume"));

        assertThat(findAll(UI.getCurrent(), Dialog.class)).hasSize(1);
    }
}

package com.example.dopamenu.ui.component;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.DopamineLevel;
import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.domain.MenuOrder;
import com.example.dopamenu.service.CurrentUser;
import com.example.dopamenu.service.MenuOrderService;
import com.example.dopamenu.ui.Dialogs;
import com.example.dopamenu.ui.UiTestSupport;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextArea;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.example.dopamenu.ui.UiTestSupport.button;
import static com.example.dopamenu.ui.UiTestSupport.click;
import static com.example.dopamenu.ui.UiTestSupport.findOne;
import static com.example.dopamenu.ui.UiTestSupport.hasText;
import static com.example.dopamenu.ui.UiTestSupport.setField;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderDialogTest {

    private final MenuOrderService orderService = mock(MenuOrderService.class);
    private final CurrentUser currentUser = mock(CurrentUser.class);
    private final Runnable onChanged = mock(Runnable.class);

    @BeforeEach
    void setUp() {
        UiTestSupport.installUi();
        when(currentUser.id()).thenReturn("user-1");
    }

    @AfterEach
    void tearDown() {
        UiTestSupport.removeUi();
    }

    private MenuItem item(Course course, String title, int minutes) {
        return new MenuItem("user-1", course, title, null, minutes,
                DopamineLevel.STEADY, course.caution());
    }

    @Test
    void beforePhaseAsksForAMoodAndShowsThePlan() {
        MenuItem item = item(Course.ENTREE, "A long walk", 45);

        OrderDialog dialog = new OrderDialog(item, orderService, currentUser, onChanged);

        assertThat(hasText(dialog, "From the Entrées course")).isTrue();
        assertThat(hasText(dialog, "A long walk")).isTrue();
        assertThat(hasText(dialog, "45 min")).isTrue();
        assertThat(hasText(dialog, "No timer needed")).isTrue();
        assertThat(hasText(dialog, "How are you feeling right now?")).isTrue();
        assertThat(hasText(dialog, "Start — 45 min")).isTrue();
        assertThat(hasText(dialog, "Not now")).isTrue();
        assertThat(hasText(dialog, "Time-boxed by design")).isFalse();
    }

    @Test
    void dessertOrdersWarnAboutTheTimer() {
        MenuItem item = item(Course.DESSERT, "One video", 10);

        OrderDialog dialog = new OrderDialog(item, orderService, currentUser, onChanged);

        assertThat(hasText(dialog, "Timer required")).isTrue();
        assertThat(hasText(dialog, "Time-boxed by design")).isTrue();
    }

    @Test
    void startingAnOrderMovesToTheRunningPhase() {
        MenuItem item = item(Course.ENTREE, "A long walk", 45);
        MenuOrder running = new MenuOrder("user-1", item, 3);
        when(orderService.start(eq("user-1"), isNull(), eq(3))).thenReturn(running);
        OrderDialog dialog = new OrderDialog(item, orderService, currentUser, onChanged);
        Dialogs.open(dialog);

        click(button(dialog, "Start — 45 min"));

        verify(orderService).start("user-1", null, 3);
        verify(onChanged).run();
        assertThat(dialog.isOpened()).isTrue();
        assertThat(hasText(dialog, "In progress · Entrées")).isTrue();
        assertThat(hasText(dialog, "left on the clock")).isTrue();
        assertThat(hasText(dialog, "+5 min")).isTrue();
        assertThat(hasText(dialog, "I'm done")).isTrue();
        assertThat(hasText(dialog, "End without logging")).isTrue();
        assertThat(UI.getCurrent().getPollInterval()).isEqualTo(1000);
    }

    @Test
    void startingWhileAnotherOrderRunsPicksTheExistingOrderBackUp() throws Exception {
        MenuItem item = item(Course.APPETIZER, "Water", 2);
        MenuOrder alreadyRunning = new MenuOrder("user-1",
                item(Course.ENTREE, "A long walk", 45), 3);
        when(orderService.start(eq("user-1"), isNull(), anyInt()))
                .thenThrow(activeOrderException(alreadyRunning));
        OrderDialog dialog = new OrderDialog(item, orderService, currentUser, onChanged);

        click(button(dialog, "Start — 2 min"));

        assertThat(hasText(dialog, "In progress · Entrées")).isTrue();
        assertThat(hasText(dialog, "A long walk")).isTrue();
        verify(onChanged).run();
    }

    @Test
    void theRunningConstructorResumesWithoutStartingANewOrder() {
        MenuOrder running = new MenuOrder("user-1",
                item(Course.SIDE, "Podcast", 25), 4);

        OrderDialog dialog = new OrderDialog(running, orderService, currentUser, onChanged);

        verify(orderService, never()).start(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyInt());
        assertThat(hasText(dialog, "In progress · Sides")).isTrue();
        assertThat(hasText(dialog, "Planned 25 min")).isTrue();
        assertThat(hasText(dialog, "left on the clock")).isTrue();
    }

    @Test
    void addingFiveMinutesExtendsTheClock() {
        MenuOrder running = new MenuOrder("user-1",
                item(Course.ENTREE, "A long walk", 45), 3);
        OrderDialog dialog = new OrderDialog(running, orderService, currentUser, onChanged);
        Span display = timerDisplay(dialog);
        String before = display.getText();

        click(button(dialog, "+5 min"));

        assertThat(display.getText()).isNotEqualTo(before);
    }

    @Test
    void anOverdueTimerCountsUpAndIsMarkedAsOver() {
        MenuOrder running = new MenuOrder("user-1",
                item(Course.APPETIZER, "Water", 1), 3);
        setField(running, "startedAt", Instant.now().minus(2, ChronoUnit.MINUTES));
        OrderDialog dialog = new OrderDialog(running, orderService, currentUser, onChanged);

        Span display = timerDisplay(dialog);

        assertThat(display.getText()).startsWith("+");
        assertThat(display.getClassNames()).contains("is-over");
    }

    @Test
    void finishingAsksHowItLandedAndLogsTheMoodAndNote() {
        MenuOrder running = new MenuOrder("user-1",
                item(Course.ENTREE, "A long walk", 45), 2);
        OrderDialog dialog = new OrderDialog(running, orderService, currentUser, onChanged);
        Dialogs.open(dialog);

        click(button(dialog, "I'm done"));

        assertThat(hasText(dialog, "Almost served")).isTrue();
        assertThat(hasText(dialog, "How did that land?")).isTrue();
        assertThat(hasText(dialog, "Log it")).isTrue();
        assertThat(hasText(dialog, "Back to timer")).isTrue();

        findOne(dialog, TextArea.class).setValue("Worth it");
        click(button(dialog, "Log it"));

        verify(orderService).complete("user-1", null, 2, "Worth it");
        verify(onChanged).run();
        assertThat(dialog.isOpened()).isFalse();
    }

    @Test
    void goingBackToTheTimerKeepsTheRunningPhase() {
        MenuOrder running = new MenuOrder("user-1",
                item(Course.ENTREE, "A long walk", 45), 2);
        OrderDialog dialog = new OrderDialog(running, orderService, currentUser, onChanged);

        click(button(dialog, "I'm done"));
        click(button(dialog, "Back to timer"));

        assertThat(hasText(dialog, "left on the clock")).isTrue();
        assertThat(hasText(dialog, "Logged. Nice work.")).isFalse();
    }

    @Test
    void endingWithoutLoggingAbandonsTheOrderAndStopsPolling() {
        MenuOrder running = new MenuOrder("user-1",
                item(Course.ENTREE, "A long walk", 45), 2);
        OrderDialog dialog = new OrderDialog(running, orderService, currentUser, onChanged);
        Dialogs.open(dialog);

        click(button(dialog, "End without logging"));

        verify(orderService).abandon("user-1", null);
        verify(onChanged).run();
        assertThat(dialog.isOpened()).isFalse();
        assertThat(UI.getCurrent().getPollInterval()).isEqualTo(-1);
    }

    private static Span timerDisplay(OrderDialog dialog) {
        return UiTestSupport.findAll(dialog, Span.class).stream()
                .filter(span -> span.getClassNames().contains("timer-display"))
                .findFirst()
                .orElseThrow();
    }

    private static MenuOrderService.ActiveOrderException activeOrderException(MenuOrder order)
            throws Exception {
        Constructor<?> constructor = MenuOrderService.ActiveOrderException.class
                .getDeclaredConstructor(MenuOrder.class);
        constructor.setAccessible(true);
        return (MenuOrderService.ActiveOrderException) constructor.newInstance(order);
    }
}

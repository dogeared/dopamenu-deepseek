package com.example.dopamenu.ui.component;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.DopamineLevel;
import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.service.CurrentUser;
import com.example.dopamenu.service.MenuOrderService;
import com.example.dopamenu.ui.Dialogs;
import com.example.dopamenu.ui.UiTestSupport;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.example.dopamenu.ui.UiTestSupport.button;
import static com.example.dopamenu.ui.UiTestSupport.click;
import static com.example.dopamenu.ui.UiTestSupport.findOne;
import static com.example.dopamenu.ui.UiTestSupport.hasText;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LogPastDialogTest {

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

    private MenuItem item(int minutes) {
        return new MenuItem("user-1", Course.ENTREE, "A long walk", null, minutes,
                DopamineLevel.BRIGHT, false);
    }

    @Test
    void theDialogExplainsItselfAndPrefillsThePlannedMinutes() {
        LogPastDialog dialog = new LogPastDialog(item(45), orderService, currentUser,
                onChanged);

        assertThat(hasText(dialog, "Already did this one?")).isTrue();
        assertThat(hasText(dialog, "A long walk")).isTrue();
        assertThat(hasText(dialog, "No timer")).isTrue();
        assertThat(hasText(dialog, "How were you feeling before?")).isTrue();
        assertThat(hasText(dialog, "How do you feel now?")).isTrue();
        assertThat(findOne(dialog, IntegerField.class).getValue()).isEqualTo(45);
    }

    @Test
    void loggingCallsTheServiceWithTheReportedMinutesAndMoods() {
        MenuItem item = item(45);
        LogPastDialog dialog = new LogPastDialog(item, orderService, currentUser, onChanged);
        Dialogs.open(dialog);

        findOne(dialog, IntegerField.class).setValue(40);
        findOne(dialog, TextArea.class).setValue("Took the long loop");
        click(button(dialog, "Add to history"));

        verify(orderService).logPast("user-1", item.getId(), 40, 3, 4,
                "Took the long loop");
        verify(onChanged).run();
        assertThat(dialog.isOpened()).isFalse();
    }

    @Test
    void aMissingDurationKeepsTheDialogOpenAndLogsNothing() {
        LogPastDialog dialog = new LogPastDialog(item(45), orderService, currentUser,
                onChanged);
        Dialogs.open(dialog);

        findOne(dialog, IntegerField.class).clear();
        click(button(dialog, "Add to history"));

        verify(orderService, never()).logPast(any(), any(), anyInt(), anyInt(), anyInt(),
                any());
        verify(onChanged, never()).run();
        assertThat(dialog.isOpened()).isTrue();
        assertThat(findOne(dialog, IntegerField.class).isInvalid()).isTrue();
    }

    @Test
    void cancelClosesWithoutLogging() {
        LogPastDialog dialog = new LogPastDialog(item(45), orderService, currentUser,
                onChanged);
        Dialogs.open(dialog);

        click(button(dialog, "Cancel"));

        verify(orderService, never()).logPast(any(), any(), anyInt(), anyInt(), anyInt(),
                any());
        assertThat(dialog.isOpened()).isFalse();
    }
}

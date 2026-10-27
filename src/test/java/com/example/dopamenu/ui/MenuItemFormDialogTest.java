package com.example.dopamenu.ui;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.DopamineLevel;
import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.service.MenuItemDraft;
import com.example.dopamenu.service.MenuItemService;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static com.example.dopamenu.ui.UiTestSupport.button;
import static com.example.dopamenu.ui.UiTestSupport.click;
import static com.example.dopamenu.ui.UiTestSupport.findAll;
import static com.example.dopamenu.ui.UiTestSupport.hasText;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class MenuItemFormDialogTest {

    private final MenuItemService service = mock(MenuItemService.class);
    private final Runnable onSaved = mock(Runnable.class);

    @BeforeEach
    void setUp() {
        UiTestSupport.installUi();
    }

    @AfterEach
    void tearDown() {
        UiTestSupport.removeUi();
    }

    @Test
    void newDishStartsWithSensibleDefaults() {
        MenuItemFormDialog dialog = new MenuItemFormDialog(service, "user-1", null, onSaved);

        assertThat(hasText(dialog, "New dish")).isTrue();
        assertThat(hasText(dialog, "Add to the menu")).isTrue();
        assertThat(title(dialog).getValue()).isEmpty();
        assertThat(minutes(dialog).getValue()).isEqualTo(10);
        assertThat(course(dialog).getValue()).isEqualTo(Course.APPETIZER);
        assertThat(level(dialog).getValue()).isEqualTo(DopamineLevel.STEADY);
        assertThat(timer(dialog).getValue()).isFalse();
    }

    @Test
    void editModePopulatesTheExistingItem() {
        MenuItem existing = new MenuItem("user-1", Course.ENTREE, "A long walk",
                "Outside", 45, DopamineLevel.BRIGHT, false);

        MenuItemFormDialog dialog = new MenuItemFormDialog(service, "user-1", existing,
                onSaved);

        assertThat(hasText(dialog, "Editing a dish")).isTrue();
        assertThat(hasText(dialog, "A long walk")).isTrue();
        assertThat(title(dialog).getValue()).isEqualTo("A long walk");
        assertThat(description(dialog).getValue()).isEqualTo("Outside");
        assertThat(minutes(dialog).getValue()).isEqualTo(45);
        assertThat(course(dialog).getValue()).isEqualTo(Course.ENTREE);
        assertThat(level(dialog).getValue()).isEqualTo(DopamineLevel.BRIGHT);
    }

    @Test
    void blankTitleAndMissingMinutesAreRejected() {
        MenuItemFormDialog dialog = new MenuItemFormDialog(service, "user-1", null, onSaved);
        Dialogs.open(dialog);
        title(dialog).setValue("   ");
        minutes(dialog).setValue(null);

        click(button(dialog, "Add to menu"));

        assertThat(title(dialog).isInvalid()).isTrue();
        assertThat(title(dialog).getErrorMessage()).isEqualTo("Give it a name");
        assertThat(minutes(dialog).isInvalid()).isTrue();
        assertThat(minutes(dialog).getErrorMessage()).isEqualTo("At least one minute");
        assertThat(dialog.isOpened()).isTrue();
        verify(service, never()).create(any(), any());
        verify(onSaved, never()).run();
    }

    @Test
    void missingCourseAndLevelAreRejected() {
        MenuItemFormDialog dialog = new MenuItemFormDialog(service, "user-1", null, onSaved);
        Dialogs.open(dialog);
        title(dialog).setValue("Water");
        course(dialog).setValue(null);
        level(dialog).setValue(null);

        click(button(dialog, "Add to menu"));

        assertThat(course(dialog).isInvalid()).isTrue();
        assertThat(level(dialog).isInvalid()).isTrue();
        assertThat(dialog.isOpened()).isTrue();
        verify(service, never()).create(any(), any());
    }

    @Test
    void validNewDishIsCreatedAndTheCallbackRuns() {
        MenuItemFormDialog dialog = new MenuItemFormDialog(service, "user-1", null, onSaved);
        Dialogs.open(dialog);
        title(dialog).setValue("  A long walk  ");
        description(dialog).setValue("  Outside  ");
        course(dialog).setValue(Course.ENTREE);
        minutes(dialog).setValue(45);
        level(dialog).setValue(DopamineLevel.BRIGHT);
        timer(dialog).setValue(true);

        click(button(dialog, "Add to menu"));

        ArgumentCaptor<MenuItemDraft> draft = ArgumentCaptor.forClass(MenuItemDraft.class);
        verify(service).create(eq("user-1"), draft.capture());
        assertThat(draft.getValue().title()).isEqualTo("  A long walk  ");
        assertThat(draft.getValue().description()).isEqualTo("  Outside  ");
        assertThat(draft.getValue().course()).isEqualTo(Course.ENTREE);
        assertThat(draft.getValue().durationMinutes()).isEqualTo(45);
        assertThat(draft.getValue().dopamineLevel()).isEqualTo(DopamineLevel.BRIGHT);
        assertThat(draft.getValue().timerRequired()).isTrue();
        verify(onSaved).run();
        assertThat(dialog.isOpened()).isFalse();
    }

    @Test
    void validEditUpdatesTheExistingItem() {
        MenuItem existing = new MenuItem("user-1", Course.APPETIZER, "Water",
                null, 2, DopamineLevel.GENTLE, false);
        MenuItemFormDialog dialog = new MenuItemFormDialog(service, "user-1", existing,
                onSaved);
        Dialogs.open(dialog);
        title(dialog).setValue("A glass of water");

        click(button(dialog, "Save changes"));

        ArgumentCaptor<MenuItemDraft> draft = ArgumentCaptor.forClass(MenuItemDraft.class);
        verify(service).update(eq("user-1"), any(), draft.capture());
        assertThat(draft.getValue().title()).isEqualTo("A glass of water");
        verify(onSaved).run();
        assertThat(dialog.isOpened()).isFalse();
    }

    @Test
    void choosingDessertForcesTheTimerOn() {
        MenuItemFormDialog dialog = new MenuItemFormDialog(service, "user-1", null, onSaved);

        course(dialog).setValue(Course.DESSERT);

        assertThat(timer(dialog).getValue()).isTrue();
        assertThat(timer(dialog).getHelperText())
                .isEqualTo("Desserts are always time-boxed.");

        course(dialog).setValue(Course.SIDE);

        assertThat(timer(dialog).getHelperText()).isNull();
    }

    @Test
    void cancelClosesWithoutSaving() {
        MenuItemFormDialog dialog = new MenuItemFormDialog(service, "user-1", null, onSaved);
        Dialogs.open(dialog);

        click(button(dialog, "Cancel"));

        assertThat(dialog.isOpened()).isFalse();
        verify(service, never()).create(any(), any());
        verify(onSaved, never()).run();
    }

    private static TextField title(MenuItemFormDialog dialog) {
        return UiTestSupport.findOne(dialog, TextField.class);
    }

    private static TextArea description(MenuItemFormDialog dialog) {
        return UiTestSupport.findOne(dialog, TextArea.class);
    }

    private static IntegerField minutes(MenuItemFormDialog dialog) {
        return UiTestSupport.findOne(dialog, IntegerField.class);
    }

    private static Checkbox timer(MenuItemFormDialog dialog) {
        return UiTestSupport.findOne(dialog, Checkbox.class);
    }

    @SuppressWarnings("unchecked")
    private static Select<Course> course(MenuItemFormDialog dialog) {
        return (Select<Course>) selectByLabel(dialog, "Course");
    }

    @SuppressWarnings("unchecked")
    private static Select<DopamineLevel> level(MenuItemFormDialog dialog) {
        return (Select<DopamineLevel>) selectByLabel(dialog, "Dopamine");
    }

    private static Select<?> selectByLabel(MenuItemFormDialog dialog, String label) {
        return findAll(dialog, Select.class).stream()
                .filter(select -> label.equals(select.getLabel()))
                .findFirst()
                .orElseThrow();
    }
}

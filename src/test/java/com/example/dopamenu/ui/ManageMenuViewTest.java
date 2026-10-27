package com.example.dopamenu.ui;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.DopamineLevel;
import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.service.CurrentUser;
import com.example.dopamenu.service.MenuItemService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.example.dopamenu.ui.UiTestSupport.button;
import static com.example.dopamenu.ui.UiTestSupport.buttonByAriaLabel;
import static com.example.dopamenu.ui.UiTestSupport.click;
import static com.example.dopamenu.ui.UiTestSupport.findAll;
import static com.example.dopamenu.ui.UiTestSupport.hasText;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManageMenuViewTest {

    private final MenuItemService menuItemService = mock(MenuItemService.class);
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

    private MenuItem item(Course course, String title, String description, boolean timer) {
        return new MenuItem("user-1", course, title, description, 30,
                DopamineLevel.STEADY, timer);
    }

    @Test
    void groupsItemsByCourseAndShowsTheirDetails() {
        List<MenuItem> items = List.of(
                item(Course.APPETIZER, "Water", null, false),
                item(Course.ENTREE, "A long walk", "Outside", false),
                item(Course.DESSERT, "One video", "Just one", true));
        when(menuItemService.ensureSeeded("user-1")).thenReturn(items);

        ManageMenuView view = new ManageMenuView(menuItemService, currentUser);

        assertThat(hasText(view, "Edit the menu.")).isTrue();
        assertThat(hasText(view, "New dish")).isTrue();
        assertThat(hasText(view, "Appetizers")).isTrue();
        assertThat(hasText(view, "Entrées")).isTrue();
        assertThat(hasText(view, "Sides")).isTrue();
        assertThat(hasText(view, "Desserts")).isTrue();
        assertThat(hasText(view, "Outside")).isTrue();
        assertThat(hasText(view, "Just one")).isTrue();
        assertThat(hasText(view, "Timer")).isTrue();
        assertThat(hasText(view, "Nothing on this course yet.")).isTrue();
        assertThat(findAll(view, MenuItemFormDialog.class)).isEmpty();
    }

    @Test
    void newDishOpensTheCreateForm() {
        when(menuItemService.ensureSeeded("user-1")).thenReturn(List.of());
        ManageMenuView view = new ManageMenuView(menuItemService, currentUser);

        click(button(view, "New dish"));

        assertThat(findAll(UI.getCurrent(), Dialog.class))
                .hasSize(1)
                .first()
                .isInstanceOf(MenuItemFormDialog.class);
        assertThat(hasText(findAll(UI.getCurrent(), Dialog.class).getFirst(),
                "Add to the menu")).isTrue();
    }

    @Test
    void editOpensTheFormPopulatedWithThatItem() {
        MenuItem item = item(Course.ENTREE, "A long walk", "Outside", false);
        when(menuItemService.ensureSeeded("user-1")).thenReturn(List.of(item));
        ManageMenuView view = new ManageMenuView(menuItemService, currentUser);

        click(buttonByAriaLabel(view, "Edit A long walk"));

        Dialog dialog = findAll(UI.getCurrent(), Dialog.class).getFirst();
        assertThat(dialog).isInstanceOf(MenuItemFormDialog.class);
        assertThat(hasText(dialog, "Editing a dish")).isTrue();
    }

    @Test
    void removeArchivesTheItemAndRerendersTheMenu() {
        MenuItem water = item(Course.APPETIZER, "Water", null, false);
        when(menuItemService.ensureSeeded("user-1"))
                .thenReturn(List.of(water), List.of());
        ManageMenuView view = new ManageMenuView(menuItemService, currentUser);

        click(buttonByAriaLabel(view, "Remove Water"));

        verify(menuItemService).archive("user-1", null);
        verify(menuItemService, times(2)).ensureSeeded("user-1");
        assertThat(hasText(view, "Water")).isFalse();
        assertThat(hasText(view, "Nothing on this course yet.")).isTrue();
    }
}

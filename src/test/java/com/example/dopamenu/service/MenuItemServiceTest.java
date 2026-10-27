package com.example.dopamenu.service;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.DopamineLevel;
import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.repository.MenuItemRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MenuItemServiceTest {

    private final MenuItemRepository repository = mock(MenuItemRepository.class);
    private final MenuItemService service = new MenuItemService(repository);

    private MenuItem item(Course course, String title) {
        return new MenuItem("user-1", course, title, null, 10,
                DopamineLevel.STEADY, course.caution());
    }

    @Test
    void seedsTheStarterMenuOnTheFirstVisit() {
        when(repository.countByOwnerSub("user-1")).thenReturn(0L);
        when(repository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.findByOwnerSubAndArchivedFalse("user-1")).thenReturn(List.of());

        service.ensureSeeded("user-1");

        verify(repository).saveAll(argThat(items ->
                StreamSupport.stream(items.spliterator(), false).count() == 16));
    }

    @Test
    void doesNotReseedAfterTheUserRetiresEveryItem() {
        // Archived rows still count, so an intentionally empty menu stays empty.
        when(repository.countByOwnerSub("user-1")).thenReturn(16L);
        when(repository.findByOwnerSubAndArchivedFalse("user-1")).thenReturn(List.of());

        List<MenuItem> menu = service.ensureSeeded("user-1");

        assertThat(menu).isEmpty();
        verify(repository, never()).saveAll(any());
    }

    @Test
    void theMenuIsSortedByCourseThenTitle() {
        MenuItem dessert = item(Course.DESSERT, "Alpha");
        MenuItem appetizerB = item(Course.APPETIZER, "banana");
        MenuItem appetizerA = item(Course.APPETIZER, "Apple");
        MenuItem entree = item(Course.ENTREE, "Walk");
        when(repository.findByOwnerSubAndArchivedFalse("user-1"))
                .thenReturn(List.of(dessert, appetizerB, entree, appetizerA));

        assertThat(service.menuFor("user-1"))
                .extracting(MenuItem::getTitle)
                .containsExactly("Apple", "banana", "Walk", "Alpha");
    }

    @Test
    void itemsForCourseKeepsOnlyThatCourse() {
        MenuItem appetizer = item(Course.APPETIZER, "Water");
        MenuItem entree = item(Course.ENTREE, "Walk");
        when(repository.findByOwnerSubAndArchivedFalse("user-1"))
                .thenReturn(List.of(appetizer, entree));

        assertThat(service.itemsForCourse("user-1", Course.ENTREE))
                .containsExactly(entree);
    }

    @Test
    void createTrimsTextAndDropsABlankDescription() {
        when(repository.save(any(MenuItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        MenuItemDraft draft = new MenuItemDraft("  A long walk  ", "   ",
                Course.ENTREE, 45, DopamineLevel.BRIGHT, false);

        MenuItem created = service.create("user-1", draft);

        assertThat(created.getTitle()).isEqualTo("A long walk");
        assertThat(created.getDescription()).isNull();
        assertThat(created.getOwnerSub()).isEqualTo("user-1");
        assertThat(created.isTimerRequired()).isFalse();
        verify(repository).save(created);
    }

    @Test
    void createKeepsATrimmedDescriptionAndHonoursTheTimerFlag() {
        when(repository.save(any(MenuItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        MenuItemDraft draft = new MenuItemDraft("Podcast", "  While cleaning  ",
                Course.SIDE, 25, DopamineLevel.STEADY, true);

        MenuItem created = service.create("user-1", draft);

        assertThat(created.getDescription()).isEqualTo("While cleaning");
        assertThat(created.isTimerRequired()).isTrue();
    }

    @Test
    void createForcesATimerForDesserts() {
        when(repository.save(any(MenuItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        MenuItemDraft draft = new MenuItemDraft("One video", null,
                Course.DESSERT, 10, DopamineLevel.INTENSE, false);

        MenuItem created = service.create("user-1", draft);

        assertThat(created.isTimerRequired()).isTrue();
    }

    @Test
    void updateChangesEveryField() {
        MenuItem existing = item(Course.APPETIZER, "Water");
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndOwnerSub(id, "user-1"))
                .thenReturn(Optional.of(existing));
        MenuItemDraft draft = new MenuItemDraft("  One video  ", "  Just one  ",
                Course.DESSERT, 10, DopamineLevel.INTENSE, false);

        MenuItem updated = service.update("user-1", id, draft);

        assertThat(updated).isSameAs(existing);
        assertThat(updated.getTitle()).isEqualTo("One video");
        assertThat(updated.getDescription()).isEqualTo("Just one");
        assertThat(updated.getCourse()).isEqualTo(Course.DESSERT);
        assertThat(updated.getDurationMinutes()).isEqualTo(10);
        assertThat(updated.getDopamineLevel()).isEqualTo(DopamineLevel.INTENSE);
        assertThat(updated.isTimerRequired()).isTrue();
    }

    @Test
    void updateClearsABlankDescription() {
        MenuItem existing = item(Course.SIDE, "Podcast");
        existing.setDescription("While cleaning");
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndOwnerSub(id, "user-1"))
                .thenReturn(Optional.of(existing));
        MenuItemDraft draft = new MenuItemDraft("Podcast", "  ",
                Course.SIDE, 25, DopamineLevel.STEADY, false);

        service.update("user-1", id, draft);

        assertThat(existing.getDescription()).isNull();
    }

    @Test
    void updateFailsForAnUnknownItem() {
        when(repository.findByIdAndOwnerSub(any(), eq("user-1")))
                .thenReturn(Optional.empty());
        MenuItemDraft draft = new MenuItemDraft("Water", null,
                Course.APPETIZER, 2, DopamineLevel.GENTLE, false);

        assertThatThrownBy(() -> service.update("user-1", UUID.randomUUID(), draft))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Menu item not found");
    }

    @Test
    void archiveRetiresTheItem() {
        MenuItem existing = item(Course.APPETIZER, "Water");
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndOwnerSub(id, "user-1"))
                .thenReturn(Optional.of(existing));

        service.archive("user-1", id);

        assertThat(existing.isArchived()).isTrue();
    }

    @Test
    void archiveIgnoresUnknownItems() {
        when(repository.findByIdAndOwnerSub(any(), eq("user-1")))
                .thenReturn(Optional.empty());

        service.archive("user-1", UUID.randomUUID());

        verify(repository).findByIdAndOwnerSub(any(), eq("user-1"));
    }

    @Test
    void theStarterMenuCoversEveryCourseAndTimeBoxesDesserts() {
        List<MenuItem> starters = MenuItemService.DefaultMenu.seedFor("user-1");

        assertThat(starters).hasSize(16);
        assertThat(starters).extracting(MenuItem::getCourse)
                .contains(Course.values());
        assertThat(starters)
                .filteredOn(item -> item.getCourse() == Course.DESSERT)
                .allMatch(MenuItem::isTimerRequired);
        assertThat(starters)
                .filteredOn(item -> item.getCourse() != Course.DESSERT)
                .noneMatch(MenuItem::isTimerRequired);
    }
}

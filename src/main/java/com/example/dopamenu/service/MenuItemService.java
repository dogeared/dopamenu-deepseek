package com.example.dopamenu.service;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.DopamineLevel;
import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.repository.MenuItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class MenuItemService {

    private final MenuItemRepository repository;

    public MenuItemService(MenuItemRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<MenuItem> menuFor(String ownerSub) {
        return repository.findByOwnerSubAndArchivedFalse(ownerSub).stream()
                .sorted(Comparator.comparingInt((MenuItem item) -> item.getCourse().order())
                        .thenComparing(MenuItem::getTitle, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MenuItem> itemsForCourse(String ownerSub, Course course) {
        return menuFor(ownerSub).stream()
                .filter(item -> item.getCourse() == course)
                .toList();
    }

    @Transactional
    public List<MenuItem> ensureSeeded(String ownerSub) {
        // Seed only the very first visit. Checking archived items as well would
        // re-create the starter menu after a user deliberately retired them all.
        if (repository.countByOwnerSub(ownerSub) == 0) {
            repository.saveAll(DefaultMenu.seedFor(ownerSub));
        }
        return menuFor(ownerSub);
    }

    @Transactional
    public MenuItem create(String ownerSub, MenuItemDraft draft) {
        MenuItem item = new MenuItem(ownerSub, draft.course(), draft.title().trim(),
                blankToNull(draft.description()), draft.durationMinutes(),
                draft.dopamineLevel(), draft.timerRequired() || draft.course().caution());
        return repository.save(item);
    }

    @Transactional
    public MenuItem update(String ownerSub, UUID id, MenuItemDraft draft) {
        MenuItem item = repository.findByIdAndOwnerSub(id, ownerSub)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
        item.setTitle(draft.title().trim());
        item.setDescription(blankToNull(draft.description()));
        item.setCourse(draft.course());
        item.setDurationMinutes(draft.durationMinutes());
        item.setDopamineLevel(draft.dopamineLevel());
        item.setTimerRequired(draft.timerRequired() || draft.course().caution());
        return item;
    }

    @Transactional
    public void archive(String ownerSub, UUID id) {
        repository.findByIdAndOwnerSub(id, ownerSub).ifPresent(MenuItem::archive);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /**
     * The starter menu offered to every new user. Users can edit or remove any
     * of these; it exists so the first visit is not an empty plate.
     */
    static final class DefaultMenu {

        private DefaultMenu() {
        }

        static List<MenuItem> seedFor(String ownerSub) {
            return List.of(
                    item(ownerSub, Course.APPETIZER, "A glass of water", 2,
                            DopamineLevel.GENTLE, "Hydrate first. It is the cheapest reset there is."),
                    item(ownerSub, Course.APPETIZER, "One song, eyes closed", 4,
                            DopamineLevel.STEADY, "No scrolling, no skipping. Just the track."),
                    item(ownerSub, Course.APPETIZER, "Fifteen jumping jacks", 3,
                            DopamineLevel.STEADY, "Enough movement to wake your body up."),
                    item(ownerSub, Course.APPETIZER, "Step into the sunlight", 5,
                            DopamineLevel.GENTLE, "Open a door, breathe, look at something far away."),
                    item(ownerSub, Course.APPETIZER, "A small, good snack", 5,
                            DopamineLevel.GENTLE, "Something you actually enjoy, not just something nearby."),

                    item(ownerSub, Course.ENTREE, "A long walk", 45,
                            DopamineLevel.BRIGHT, "Leave the headphones behind for the first ten minutes."),
                    item(ownerSub, Course.ENTREE, "Cook something from scratch", 60,
                            DopamineLevel.BRIGHT, "A real recipe, a real pan, a real mess."),
                    item(ownerSub, Course.ENTREE, "Play an instrument", 40,
                            DopamineLevel.BRIGHT, "Practice, noodle, or learn four bars of something new."),
                    item(ownerSub, Course.ENTREE, "Journal three pages", 30,
                            DopamineLevel.STEADY, "Keep the pen moving, even when it gets boring."),
                    item(ownerSub, Course.ENTREE, "A yoga flow", 30,
                            DopamineLevel.STEADY, "Move slowly enough to notice you are moving."),

                    item(ownerSub, Course.SIDE, "Podcast while cleaning", 25,
                            DopamineLevel.STEADY, "Pair a chore you avoid with a story you want."),
                    item(ownerSub, Course.SIDE, "A familiar album for admin work", 30,
                            DopamineLevel.GENTLE, "Music you know too well to get distracted by."),
                    item(ownerSub, Course.SIDE, "Fidget toy, deep work block", 45,
                            DopamineLevel.GENTLE, "Give the restless part of your brain a job."),

                    item(ownerSub, Course.DESSERT, "Ten minutes of social scroll", 10,
                            DopamineLevel.INTENSE, "Set the timer before you open the app."),
                    item(ownerSub, Course.DESSERT, "One YouTube video", 10,
                            DopamineLevel.BRIGHT, "One. Not the next one, not the sidebar."),
                    item(ownerSub, Course.DESSERT, "A phone game round", 15,
                            DopamineLevel.INTENSE, "A single round, then out."));
        }

        private static MenuItem item(String ownerSub, Course course, String title,
                int minutes, DopamineLevel level, String description) {
            return new MenuItem(ownerSub, course, title, description, minutes, level,
                    course.caution());
        }
    }
}

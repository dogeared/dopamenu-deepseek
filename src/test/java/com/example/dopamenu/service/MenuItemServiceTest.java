package com.example.dopamenu.service;

import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.repository.MenuItemRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MenuItemServiceTest {

    private final MenuItemRepository repository = mock(MenuItemRepository.class);
    private final MenuItemService service = new MenuItemService(repository);

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
}

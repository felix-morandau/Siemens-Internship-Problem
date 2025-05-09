package com.siemens.internship.service;

import com.siemens.internship.config.ItemNotFoundException;
import com.siemens.internship.dto.UpdateItemDTO;
import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.dto.CreateItemDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private final Executor directExecutor = Runnable::run;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(itemService, "taskExecutor", directExecutor);
    }

    @Test
    void findAllReturnsAllItems() {
        List<Item> items = Arrays.asList(
                new Item(1L, "A", "Desc", "PENDING", "a@a.com"),
                new Item(2L, "B", "Desc2", "DONE", "b@b.com")
        );
        when(itemRepository.findAll()).thenReturn(items);

        List<Item> result = itemService.findAll();

        assertEquals(items, result);
        verify(itemRepository).findAll();
    }

    @Test
    void findByIdExistingIdReturnsItem() {
        Item item = new Item(1L, "A", "Desc", "PENDING", "a@a.com");
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Item result = itemService.findById(1L);

        assertEquals(item, result);
    }

    @Test
    void findByIdNotFoundThrowsException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.findById(1L));
    }

    @Test
    void createItemReturnsItem() {
        CreateItemDTO dto = new CreateItemDTO("Name", "Desc", "NEW", "test@ex.com");
        Item saved = new Item();
        saved.setId(42L);
        saved.setName(dto.getName());
        saved.setDescription(dto.getDescription());
        saved.setStatus(dto.getStatus());
        saved.setEmail(dto.getEmail());

        when(itemRepository.save(any(Item.class))).thenReturn(saved);

        Item result = itemService.createItem(dto);

        assertNotNull(result, "Service should return the saved item");
        assertEquals(42L, result.getId());
        assertEquals("Name", result.getName());
        assertEquals("Desc", result.getDescription());
        assertEquals("NEW", result.getStatus());
        assertEquals("test@ex.com", result.getEmail());

        verify(itemRepository, times(1)).save(any(Item.class));
    }


    @Test
    void updateItemExistingIdUpdatesAndReturns() {
        Item existing = new Item(1L, "Old", "OldDesc", "OLD", "old@ex.com");
        when(itemRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateItemDTO dto = new UpdateItemDTO("New", "NewDesc", "NEW", "new@ex.com");
        Item result = itemService.updateItem(1L, dto);

        assertEquals("New", result.getName());
        assertEquals("NewDesc", result.getDescription());
        assertEquals("NEW", result.getStatus());
        assertEquals("new@ex.com", result.getEmail());
        verify(itemRepository).save(existing);
    }

    @Test
    void updateItemNotFoundThrowsException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());
        UpdateItemDTO dto = new UpdateItemDTO("N", null, "S", "e@e.com");

        assertThrows(ItemNotFoundException.class, () -> itemService.updateItem(1L, dto));
    }

    @Test
    void deleteByIdExistingIdDeletes() {
        Item existing = new Item(1L, "Name", null, "S", "e@e.com");
        when(itemRepository.findById(1L)).thenReturn(Optional.of(existing));

        itemService.deleteById(1L);

        verify(itemRepository).delete(existing);
    }

    @Test
    void deleteByIdNotFoundThrowsException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.deleteById(1L));
    }

    @Test
    void processItemsAsyncAllSuccessReturnsProcessedList() {
        List<Long> ids = Arrays.asList(1L, 2L);
        when(itemRepository.findAllIds()).thenReturn(ids);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(new Item(1L, "A", null, "PENDING", "a@a.com")));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(new Item(2L, "B", null, "PENDING", "b@b.com")));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> processed = future.join();

        assertEquals(2, processed.size());
        processed.forEach(item -> assertEquals("PROCESSED", item.getStatus()));
    }

    @Test
    void processItemsAsyncMissingItemFailsFuture() {
        List<Long> ids = List.of(1L);
        when(itemRepository.findAllIds()).thenReturn(ids);
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        CompletableFuture<List<Item>> future = itemService.processItemsAsync();

        assertThrows(CompletionException.class, future::join);
    }
}

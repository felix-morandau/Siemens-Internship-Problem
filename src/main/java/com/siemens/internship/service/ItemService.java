package com.siemens.internship.service;

import com.siemens.internship.config.ItemNotFoundException;
import com.siemens.internship.dto.CreateItemDTO;
import com.siemens.internship.dto.UpdateItemDTO;
import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;

    @Qualifier("taskExecutor")
    private Executor taskExecutor;
    private AtomicInteger processedCount = new AtomicInteger(0);
    private List<Item> processedItems = Collections.synchronizedList(new ArrayList<>());

    /**
     * Retrieves all Items from the database.
     *
     * @return a List of all stored Item objects
     */
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    /**
     * Looks up a single Item by its ID.
     *
     * @param id the database identifier of the Item
     * @return the Item with the given ID
     * @throws ItemNotFoundException if no Item with the given ID exists
     */
    public Item findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
    }

    /**
     * Creates a new Item from the provided DTO and saves it.
     * <p>
     * The CreateItemDTO is validated before this method is called
     * (e.g. @NotBlank on name, @Pattern on email, etc.).
     * </p>
     *
     * @param dto the data transfer object containing name, optional description, status, and email
     * @return the newly created and persisted Item
     */
    public Item createItem(CreateItemDTO dto) {
        Item item = new Item();
        item.setName(dto.getName());

        if (dto.getDescription() != null) {
            item.setDescription(dto.getDescription());
        }
        item.setStatus(dto.getStatus());
        item.setEmail(dto.getEmail());

        return itemRepository.save(item);
    }

    /**
     * Updates an existing Item with new values from the provided DTO.
     * <p>
     * First retrieves the Item by ID (throwing ItemNotFoundException if absent),
     * then applies any changed fields from the DTO, and saves.
     * </p>
     *
     * @param id  the ID of the Item to update
     * @param dto the data transfer object containing updated values (name, description, status, email)
     * @return the updated and persisted Item
     * @throws ItemNotFoundException if no Item with the given ID exists
     */
    public Item updateItem(Long id, @Valid UpdateItemDTO dto) {
        Item item = findById(id);
        item.setName(dto.getName());

        if (dto.getDescription() != null) {
            item.setDescription(dto.getDescription());
        }
        item.setStatus(dto.getStatus());
        item.setEmail(dto.getEmail());

        return itemRepository.save(item);
    }

    /**
     * Deletes the Item with the given ID from the database.
     * <p>
     * First retrieves the Item by ID (throwing ItemNotFoundException if absent),
     * then performs the delete operation.
     * </p>
     *
     * @param id the ID of the Item to delete
     * @throws ItemNotFoundException if no Item with the given ID exists
     */
    public void deleteById(Long id) {
        Item item = findById(id);
        itemRepository.delete(item);
    }

    /**
     * Processes all items in parallel and marks them as PROCESSED.
     * Returns a CompletableFuture that completes only after every item is done.
     * Changes from the original:
     *  <li> Waits for all tasks using CompletableFuture.allOf(...) </li>
     *  <li> Uses a thread-safe list and AtomicInteger for shared resources </li>
     *  <li> Wraps errors so failures propagate instead of being hidden </li>
     *  <li> Runs everything on the same Spring taskExecutor </li>
     *
     * @return a future holding the list of processed items, or an exception if any task fails
     */

    @Async("taskExecutor")
    public CompletableFuture<List<Item>> processItemsAsync() {
        List<Long> itemIds = itemRepository.findAllIds();

        List<CompletableFuture<Void>> futures = itemIds.stream()
                .map(id -> CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(100);

                        Item item = itemRepository.findById(id)
                                .orElseThrow(() -> new IllegalStateException("Item " + id + " not found"));

                        item.setStatus("PROCESSED");
                        itemRepository.save(item);

                        processedItems.add(item);
                        processedCount.incrementAndGet();

                    } catch (Exception ex) {
                        throw new RuntimeException("Error processing item " + id, ex);
                    }
                }, taskExecutor))
                .toList();

        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(voidResult -> {
                    log.info("Completed processing {} items", processedCount.get());
                    return processedItems;
                });
    }

}


package com.siemens.internship.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.internship.dto.CreateItemDTO;
import com.siemens.internship.dto.UpdateItemDTO;
import com.siemens.internship.model.Item;
import com.siemens.internship.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void getAllItems_ReturnsList() throws Exception {
        List<Item> items = Arrays.asList(
                new Item(1L, "A", "DescA", "PENDING", "a@a.com"),
                new Item(2L, "B", "DescB", "DONE", "b@b.com")
        );
        when(itemService.findAll()).thenReturn(items);

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("A"))
                .andExpect(jsonPath("$[1].status").value("DONE"));

        verify(itemService).findAll();
    }

    @Test
    void createItem_ReturnsCreated() throws Exception {
        CreateItemDTO dto = new CreateItemDTO("New", "Desc", "PENDING", "new@ex.com");
        Item created = new Item(3L, "New", "Desc", "PENDING", "new@ex.com");
        when(itemService.createItem(any(CreateItemDTO.class))).thenReturn(created);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.email").value("new@ex.com"));

        verify(itemService).createItem(any(CreateItemDTO.class));
    }

    @Test
    void createItem_InvalidEmail_ReturnsBadRequest1() throws Exception {
        CreateItemDTO dto = new CreateItemDTO("Name", "Desc", "PENDING", "not-an-email");

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Invalid email format"));
    }

    @Test
    void createItem_InvalidEmail_ReturnsBadRequest2() throws Exception {
        CreateItemDTO dto = new CreateItemDTO("Name", "Desc", "PENDING", "wrongemail@gmail.r");

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Invalid email format"));
    }


    @Test
    void getItemById_ReturnsItem() throws Exception {
        Item item = new Item(4L, "X", null, "DONE", "x@ex.com");
        when(itemService.findById(4L)).thenReturn(item);

        mockMvc.perform(get("/api/items/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.name").value("X"))
                .andExpect(jsonPath("$.status").value("DONE"));

        verify(itemService).findById(4L);
    }

    @Test
    void updateItem_ReturnsUpdated() throws Exception {
        UpdateItemDTO dto = new UpdateItemDTO("Upd", "DescU", "DONE", "u@ex.com");
        Item updated = new Item(5L, "Upd", "DescU", "DONE", "u@ex.com");
        when(itemService.updateItem(eq(5L), any(UpdateItemDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/items/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Upd"));

        verify(itemService).updateItem(eq(5L), any(UpdateItemDTO.class));
    }

    @Test
    void deleteItem_ReturnsNoContent() throws Exception {
        doNothing().when(itemService).deleteById(6L);

        mockMvc.perform(delete("/api/items/6"))
                .andExpect(status().isNoContent());

        verify(itemService).deleteById(6L);
    }

    @Test
    void processItems_ReturnsItems() throws Exception {
        List<Item> processed = Arrays.asList(
                new Item(7L, "A", null, "PROCESSED", "a@a.com"),
                new Item(8L, "B", null, "PROCESSED", "b@b.com")
        );
        when(itemService.processItemsAsync()).thenReturn(CompletableFuture.completedFuture(processed));

        mockMvc.perform(get("/api/items/process"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].status").value("PROCESSED"));

        verify(itemService).processItemsAsync();
    }
}

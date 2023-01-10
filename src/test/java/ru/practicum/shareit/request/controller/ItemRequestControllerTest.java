package ru.practicum.shareit.request.controller;

import java.util.List;
import java.time.LocalDateTime;
import org.mockito.Mockito;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.practicum.shareit.utils.LocalDateTimeAdapter;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import javax.persistence.EntityNotFoundException;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    private UserDto owner;
    private UserDto requestor;
    private ItemRequest request;
    private ItemRequestDto dto;
    private Gson gson;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemRequestService requestService;

    @BeforeEach
    void init() {
        owner = new UserDto(1L, "Nikolas", "nik@mail.ru");
        requestor = new UserDto(2L, "Djon", "djony@mail.ru");
        request = new ItemRequest(1L, "Rent drill on 2 days", LocalDateTime.now().plusDays(2), UserMapper.toUser(requestor));
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .serializeNulls()
                .create();
        dto = ItemRequestMapper.toItemRequestDto(request);
    }

    @AfterEach
    void tearDown() {
        owner = null;
        requestor = null;
        request = null;
        gson = null;
        dto = null;
    }

    @Test
    @DisplayName("Send GET request /requests/{id}")
    void findById() throws Exception {
        Mockito.when(requestService.findById(owner.getId(), request.getId())).thenReturn(dto);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/requests/{id}", request.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber());

        Mockito.verify(requestService, Mockito.times(1)).findById(owner.getId(), request.getId());
    }

    @Test
    @DisplayName("Send GET request /requests/{id}")
    void findByNotValidId() throws Exception {
        Mockito.when(requestService.findById(owner.getId(), request.getId())).thenThrow(EntityNotFoundException.class);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/requests/{id}", request.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(requestService, Mockito.times(1)).findById(owner.getId(), request.getId());
    }

    @Test
    @DisplayName("Send GET request /requests")
    void findAll() throws Exception {
        Mockito.when(requestService.findAll(requestor.getId())).thenReturn(List.of(dto));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/requests", request.getId())
                        .header("X-Sharer-User-Id", requestor.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").isNumber());

        Mockito.verify(requestService, Mockito.times(1)).findAll(requestor.getId());
    }

    @Test
    @DisplayName("Send GET request /requests/all?from={from}&size={size}")
    void findByPage() throws Exception {
        Mockito.when(requestService.findByPage(owner.getId(), 0, 10)).thenReturn(List.of(dto));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/requests/all?from={from}&size={size}", 0, 10)
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").isNumber());

        Mockito.verify(requestService, Mockito.times(1)).findByPage(owner.getId(), 0, 10);
    }

    @Test
    @DisplayName("Send POST request /requests")
    void save() throws Exception {
        Mockito.when(requestService.save(Mockito.any(), Mockito.anyLong())).thenReturn(dto);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/requests")
                        .header("X-Sharer-User-Id", requestor.getId())
                        .content(gson.toJson(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(requestService, Mockito.times(1)).save(Mockito.any(), Mockito.anyLong());
    }

    @Test
    @DisplayName("Send DELETE request /requests/{id}")
    void deleteById() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/requests/{id}", request.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(requestService, Mockito.times(1)).deleteById(owner.getId(), dto.getId());
    }
}

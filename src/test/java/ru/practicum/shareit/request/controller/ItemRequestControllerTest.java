package ru.practicum.shareit.request.controller;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import org.mockito.Mockito;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.MyPageRequest;
import ru.practicum.shareit.utils.LocalDateTimeAdapter;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.repo.ItemRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ItemRequestControllerTest {
    private User owner;
    private User requestor;
    private ItemRequest request;
    private Gson gson;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private ItemRequestRepository requestRepository;

    @BeforeEach
    void init() {
        owner = new User(1L, "Nikolas", "nik@mail.ru");
        requestor = new User(2L, "Djon", "djony@mail.ru");
        request = new ItemRequest(1L, "Rent drill on 2 days", LocalDateTime.now().plusDays(2), requestor);
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .serializeNulls()
                .create();
    }

    @AfterEach
    void tearDown() {
        owner = null;
        requestor = null;
        request = null;
        gson = null;
        userRepository.deleteAll();
        requestRepository.deleteAll();
    }

    @Test
    @DisplayName("Send GET request /requests/{id}")
    void findById() throws Exception {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/requests/{id}", request.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).findById(request.getId());
    }

    @Test
    @DisplayName("Send GET request /requests/{id}")
    void findByNotValidId() throws Exception {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/requests/{id}", request.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }

    @Test
    @DisplayName("Send GET request /requests")
    void findAll() throws Exception {
        Mockito.when(userRepository.findById(requestor.getId())).thenReturn(Optional.of(requestor));
        Mockito.when(requestRepository.findAllByRequestorId(requestor.getId())).thenReturn(List.of(request));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/requests", request.getId())
                        .header("X-Sharer-User-Id", requestor.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").isNumber());

        Mockito.verify(userRepository, Mockito.times(1)).findById(requestor.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).findAllByRequestorId(requestor.getId());
    }

    @Test
    @DisplayName("Send GET request /requests/all?from={from}&size={size}")
    void findByPage() throws Exception {
        MyPageRequest pageRequest = new MyPageRequest(0, 10, Sort.by(Sort.Direction.DESC, "created"));
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(requestRepository.findAllByRequestorIdNot(owner.getId(), pageRequest)).thenReturn(List.of(request));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/requests/all?from={from}&size={size}", 0, 10)
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").isNumber());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).findAllByRequestorIdNot(owner.getId(), pageRequest);
    }

    @Test
    @DisplayName("Send POST request /requests")
    void save() throws Exception {
        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);
        Mockito.when(userRepository.findById(requestor.getId())).thenReturn(Optional.of(requestor));
        Mockito.when(requestRepository.save(Mockito.any())).thenReturn(request);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/requests")
                        .header("X-Sharer-User-Id", requestor.getId())
                        .content(gson.toJson(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(userRepository, Mockito.times(1)).findById(requestor.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    @DisplayName("Send DELETE request /requests/{id}")
    void deleteById() throws Exception {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/requests/{id}", request.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).findById(request.getId());
    }
}

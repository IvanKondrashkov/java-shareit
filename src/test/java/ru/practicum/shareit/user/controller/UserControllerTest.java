package ru.practicum.shareit.user.controller;

import java.util.List;
import org.mockito.Mockito;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
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

@WebMvcTest(UserController.class)
class UserControllerTest {
    private User user;
    private UserDto dto;
    private Gson gson;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;

    @BeforeEach
    void init() {
        user = new User(1L, "Nikolas", "nik@mail.ru");
        dto = UserMapper.toUserDto(user);
        gson = new GsonBuilder()
                .serializeNulls()
                .create();
    }

    @AfterEach
    void tearDown() {
        user = null;
        dto = null;
        gson = null;
    }

    @Test
    @DisplayName("Send GET request /users/{id}")
    void findById() throws Exception {
        Mockito.when(userService.findById(user.getId())).thenReturn(dto);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber());

        Mockito.verify(userService, Mockito.times(1)).findById(user.getId());
    }

    @Test
    @DisplayName("Send GET request /users/{id}")
    void findByNotValidId() throws Exception {
        Mockito.when(userService.findById(user.getId())).thenThrow(EntityNotFoundException.class);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(userService, Mockito.times(1)).findById(user.getId());
    }

    @Test
    @DisplayName("Send GET request /users")
    void findAll() throws Exception {
        Mockito.when(userService.findAll()).thenReturn(List.of(dto));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").isNumber());

        Mockito.verify(userService, Mockito.times(1)).findAll();
    }

    @Test
    @DisplayName("Send POST request /users")
    void save() throws Exception {
        Mockito.when(userService.save(Mockito.any())).thenReturn(dto);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(dto)))
                .andExpect(status().isOk());

        Mockito.verify(userService, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    @DisplayName("Send PATCH request /users/{id}")
    void update() throws Exception {
        dto.setName("Mike");
        Mockito.when(userService.update(Mockito.any(), Mockito.anyLong())).thenReturn(dto);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(dto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Mike"));

        Mockito.verify(userService, Mockito.times(1)).update(Mockito.any(), Mockito.anyLong());
    }

    @Test
    @DisplayName("Send DELETE request users/{id}")
    void deleteById() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(userService, Mockito.times(1)).deleteById(user.getId());
    }
}

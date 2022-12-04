package ru.practicum.shareit.user.controller;

import java.util.List;
import org.mockito.Mockito;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dao.UserStorage;
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
class UserControllerTest {
    private User user;
    private Gson gson;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserStorage userStorage;

    @BeforeEach
    void init() {
        user = new User(1L, "Nikolas", "nik@mail.ru");
        gson = new GsonBuilder()
                .serializeNulls()
                .create();
    }

    @AfterEach
    void tearDown() {
        user = null;
        gson = null;
        mockMvc = null;
    }

    @Test
    @DisplayName("Send GET request /users/{id}")
    void findById() throws Exception {
        Mockito.when(userStorage.findById(user.getId())).thenReturn(user);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("Send GET request /users")
    void findAll() throws Exception {
        Mockito.when(userStorage.findAll()).thenReturn(List.of(user));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").isNumber());
    }

    @Test
    @DisplayName("Send POST request /users")
    void save() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(user)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Send PATCH request /users/{id}")
    void update() throws Exception {
        Mockito.when(userStorage.update(user, user.getId())).thenReturn(user);
        user.setName("Mike");

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(user)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Mike"));
    }

    @Test
    @DisplayName("Send DELETE request users/{id}")
    void deleteById() throws Exception {
        Mockito.when(userStorage.findById(user.getId())).thenReturn(user);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

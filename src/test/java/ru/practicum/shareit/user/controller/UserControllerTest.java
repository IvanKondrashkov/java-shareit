package ru.practicum.shareit.user.controller;

import java.util.List;
import java.util.Optional;
import org.mockito.Mockito;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.practicum.shareit.user.model.User;
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
import ru.practicum.shareit.user.repo.UserRepository;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    private User user;
    private Gson gson;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserRepository userRepository;

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
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Send GET request /users/{id}")
    void findById() throws Exception {
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber());

        Mockito.verify(userRepository, Mockito.times(1)).findById(user.getId());
    }

    @Test
    @DisplayName("Send GET request /users/{id}")
    void findByNotValidId() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Send GET request /users")
    void findAll() throws Exception {
        Mockito.when(userRepository.findAll()).thenReturn(List.of(user));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").isNumber());

        Mockito.verify(userRepository, Mockito.times(1)).findAll();
    }

    @Test
    @DisplayName("Send POST request /users")
    void save() throws Exception {
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(user)))
                .andExpect(status().isOk());

        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    @DisplayName("Send PATCH request /users/{id}")
    void update() throws Exception {
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        user.setName("Mike");

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(user)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Mike"));

        Mockito.verify(userRepository, Mockito.times(1)).findById(user.getId());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    @DisplayName("Send DELETE request users/{id}")
    void deleteById() throws Exception {
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(userRepository, Mockito.times(1)).deleteById(user.getId());
    }
}

package ru.practicum.shareit.item.controller;

import java.util.List;
import org.mockito.Mockito;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.dao.ItemStorage;
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
class ItemControllerTest {
    private User owner;
    private Item item;
    private Gson gson;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserStorage userStorage;
    @MockBean
    private ItemStorage itemStorage;

    @BeforeEach
    void init() {
        owner = new User(1L,
                "Nikolas",
                "nik@mail.ru"
        );

        item = new Item(1L,
                "Drill",
                "Cordless drill",
                true,
                owner
        );

        gson = new GsonBuilder()
                .serializeNulls()
                .create();
    }

    @AfterEach
    void tearDown() {
        owner = null;
        item = null;
        gson = null;
        mockMvc = null;
    }

    @Test
    @DisplayName("Send GET request /items/{id}")
    void findById() throws Exception {
        Mockito.when(itemStorage.findById(owner.getId(), item.getId())).thenReturn(item);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("Send GET request /items/search?text={text}")
    void findByKeyWords() throws Exception {
        final String text = "Drill";
        Mockito.when(itemStorage.findByKeyWord(owner.getId(), text)).thenReturn(List.of(item));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/items/search?text={text}", text)
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Send GET request /items")
    void findAll() throws Exception {
        Mockito.when(itemStorage.findAll(owner.getId())).thenReturn(List.of(item));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/items")
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").isNumber());
    }

    @Test
    @DisplayName("Send POST request /items")
    void save() throws Exception {
        Mockito.when(userStorage.findById(owner.getId())).thenReturn(owner);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/items")
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(item)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Send PATCH request /items/{id}")
    void update() throws Exception {
        Mockito.when(userStorage.findById(owner.getId())).thenReturn(owner);
        Mockito.when(itemStorage.update(item, owner.getId(), item.getId())).thenReturn(item);
        item.setName("Drill 2000");
        item.setAvailable(false);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(item)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Drill 2000"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.available").value(false));
    }

    @Test
    @DisplayName("Send DELETE request items/{id}")
    void deleteById() throws Exception {
        Mockito.when(itemStorage.findById(owner.getId(), item.getId())).thenReturn(item);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

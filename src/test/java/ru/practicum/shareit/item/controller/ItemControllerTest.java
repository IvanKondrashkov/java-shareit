package ru.practicum.shareit.item.controller;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import org.mockito.Mockito;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.repo.CommentRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;
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
    private User booker;
    private Item item;
    private Booking booking;
    private Comment comment;
    private Gson gson;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private ItemRepository itemRepository;
    @MockBean
    private BookingRepository bookingRepository;
    @MockBean
    private CommentRepository commentRepository;

    @BeforeEach
    void init() {
        owner = new User(1L, "Nikolas", "nik@mail.ru");
        booker = new User(2L, "Djon", "djony@mail.ru");
        item = new Item(1L, "Drill", "Cordless drill", true, owner);
        booking = new Booking(1L, LocalDateTime.now().minusDays(15), LocalDateTime.now().minusDays(10), BookingStatus.APPROVED, item, booker);
        comment = new Comment(1L, "Good drill!", LocalDateTime.now(), item, booker);
        gson = new GsonBuilder()
                .serializeNulls()
                .create();
    }

    @AfterEach
    void tearDown() {
        owner = null;
        booker = null;
        item = null;
        booking = null;
        comment = null;
        gson = null;
        userRepository.deleteAll();
        itemRepository.deleteAll();
        bookingRepository.deleteAll();
        commentRepository.deleteAll();
    }

    @Test
    @DisplayName("Send GET request /items/{id}")
    void findById() throws Exception {
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(owner));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(item));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("Send GET request /items/search?text={text}")
    void findAllByText() throws Exception {
        final String text = "Drill";
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(owner));
        Mockito.when(itemRepository.findAllByText(text)).thenReturn(List.of(item));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/items/search?text={text}", text)
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Send GET request /items")
    void findAll() throws Exception {
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(owner));
        Mockito.when(itemRepository.findAllByOwnerId(Mockito.any())).thenReturn(List.of(item));

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
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(owner));
        Mockito.when(itemRepository.save(Mockito.any())).thenReturn(item);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/items")
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(item)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Send POST request /items/{id}/comment")
    void saveComment() throws Exception {
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(owner));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(item));
        Mockito.when(bookingRepository.findAllByItemId(Mockito.any())).thenReturn(List.of(booking));
        Mockito.when(commentRepository.save(Mockito.any())).thenReturn(comment);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/items/{id}/comment", item.getId())
                        .header("X-Sharer-User-Id", booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(comment)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Send PATCH request /items/{id}")
    void update() throws Exception {
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(owner));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(item));
        item.setName("Drill 2000");
        item.setAvailable(false);
        item.setDescription("Very good drill!");

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(item)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Drill 2000"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.available").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Very good drill!"));
    }

    @Test
    @DisplayName("Send DELETE request items/{id}")
    void deleteById() throws Exception {
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(owner));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(item));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

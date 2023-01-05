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
        item = Item.builder()
                .id(1L)
                .name("Drill")
                .description("Cordless drill")
                .available(true)
                .owner(owner)
                .build();
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
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
    }

    @Test
    @DisplayName("Send GET request /items/{id}")
    void findByNotValidId() throws Exception {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }

    @Test
    @DisplayName("Send GET request /items/search?text={text}")
    void findAllByText() throws Exception {
        final String text = "Drill";
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findAllByText(text)).thenReturn(List.of(item));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/items/search?text={text}", text)
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findAllByText(text);
    }

    @Test
    @DisplayName("Send GET request /items")
    void findAll() throws Exception {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findAllByOwnerId(owner.getId())).thenReturn(List.of(item));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/items")
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").isNumber());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findAllByOwnerId(owner.getId());
    }

    @Test
    @DisplayName("Send POST request /items")
    void save() throws Exception {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.save(Mockito.any())).thenReturn(item);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/items")
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(item)))
                .andExpect(status().isOk());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    @DisplayName("Send POST request /items/{id}/comment")
    void saveComment() throws Exception {
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findAllByItemId(item.getId())).thenReturn(List.of(booking));
        Mockito.when(commentRepository.save(Mockito.any())).thenReturn(comment);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/items/{id}/comment", item.getId())
                        .header("X-Sharer-User-Id", booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(comment)))
                .andExpect(status().isOk());

        Mockito.verify(userRepository, Mockito.times(1)).findById(booker.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findAllByItemId(item.getId());
        Mockito.verify(commentRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    @DisplayName("Send POST request /items/{id}/comment")
    void saveCommentByNotBooker() throws Exception {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findAllByItemId(item.getId())).thenReturn(List.of(booking));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/items/{id}/comment", item.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(comment)))
                .andExpect(status().isBadRequest());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findAllByItemId(item.getId());
    }

    @Test
    @DisplayName("Send PATCH request /items/{id}")
    void update() throws Exception {
        item.setName("Drill 2000");
        item.setAvailable(false);
        item.setDescription("Very good drill!");
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(item)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Drill 2000"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.available").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Very good drill!"));

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
    }

    @Test
    @DisplayName("Send PATCH request /items/{id}")
    void updateByNotOwner() throws Exception {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(new User()));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(item)))
                .andExpect(status().isNotFound());

        Mockito.verify(userRepository, Mockito.times(1)).findById(2L);
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
    }

    @Test
    @DisplayName("Send DELETE request items/{id}")
    void deleteById() throws Exception {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
    }
}

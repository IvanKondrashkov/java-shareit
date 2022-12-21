package ru.practicum.shareit.booking.controller;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import org.mockito.Mockito;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.utils.LocalDateTimeAdapter;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repo.BookingRepository;
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
class BookingControllerTest {
    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private Gson gson;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private ItemRepository itemRepository;
    @MockBean
    private BookingRepository bookingRepository;

    @BeforeEach
    void init() {
        owner = new User(1L, "Nikolas", "nik@mail.ru");
        booker = new User(2L, "Djon", "djony@mail.ru");
        item = new Item(1L, "Drill", "Cordless drill", true, owner);
        booking = new Booking(1L, LocalDateTime.now().minusDays(15), LocalDateTime.now().minusDays(10), BookingStatus.WAITING, item, booker);
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .serializeNulls()
                .create();
    }

    @AfterEach
    void tearDown() {
        owner = null;
        booker = null;
        item = null;
        booking = null;
        gson = null;
        userRepository.deleteAll();
        itemRepository.deleteAll();
        bookingRepository.deleteAll();
    }

    @Test
    @DisplayName("Send GET request /bookings/{id}")
    void findById() throws Exception {
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(owner));
        Mockito.when(bookingRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(booking));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/bookings/{id}", item.getId())
                        .header("X-Sharer-User-Id", booker.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("Send GET request /bookings?state={state}")
    void findAllByBookerId() throws Exception {
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(owner));
        Mockito.when(bookingRepository.findAllByBookerId(Mockito.any(), Mockito.any())).thenReturn(List.of(booking));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/bookings?state={state}", BookingState.PAST)
                        .header("X-Sharer-User-Id", booker.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").isNumber());
    }

    @Test
    @DisplayName("Send GET request /bookings/owner?state={state}")
    void findAllByItemOwnerId() throws Exception {
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(owner));
        Mockito.when(bookingRepository.findAllByItemOwnerId(Mockito.any(), Mockito.any())).thenReturn(List.of(booking));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/bookings/owner?state={state}", BookingState.PAST)
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").isNumber());
    }

    @Test
    @DisplayName("Send POST request /bookings")
    void save() throws Exception {
        booking = new Booking(2L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.WAITING, item, booker);
        final BookingDto dto = BookingMapper.toBookingDto(booking);

        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(booker));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(item));
        Mockito.when(bookingRepository.save(Mockito.any())).thenReturn(booking);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/bookings")
                        .header("X-Sharer-User-Id", booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Send PATCH request /bookings/{id}?approved={approved}")
    void update() throws Exception {
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(owner));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(item));
        Mockito.when(bookingRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(booking));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/bookings/{id}?approved={approved}", booking.getId(), "false")
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(BookingStatus.REJECTED.name()));
    }

    @Test
    @DisplayName("Send DELETE request /bookings/{id}")
    void deleteById() throws Exception {
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(owner));
        Mockito.when(bookingRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(booking));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/bookings/{id}", booking.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

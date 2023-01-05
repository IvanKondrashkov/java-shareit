package ru.practicum.shareit.booking.controller;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import org.mockito.Mockito;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.MyPageRequest;
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
        item = Item.builder()
                .id(1L)
                .name("Drill")
                .description("Cordless drill")
                .available(true)
                .owner(owner)
                .build();
        booking = new Booking(1L, LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(5), BookingStatus.WAITING, item, booker);
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
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/bookings/{id}", booking.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findById(booking.getId());
    }

    @Test
    @DisplayName("Send GET request /bookings/{id}")
    void findByNotValidId() throws Exception {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/bookings/{id}", booking.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }

    @Test
    @DisplayName("Send GET request /bookings?state={state}&from={from}&size={size}")
    void findAllByBookerId() throws Exception {
        MyPageRequest pageRequest = new MyPageRequest(0, 10, Sort.by(Sort.Direction.DESC, "start"));
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(bookingRepository.findAllByBookerId(booker.getId(), pageRequest)).thenReturn(List.of(booking));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/bookings?state={state}&from={from}&size={size}", BookingState.PAST, 0, 10)
                        .header("X-Sharer-User-Id", booker.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").isNumber());

        Mockito.verify(userRepository, Mockito.times(1)).findById(booker.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findAllByBookerId(booker.getId(), pageRequest);
    }

    @Test
    @DisplayName("Send GET request /bookings?state={state}&from={from}&size={size}")
    void findAllByBookerIdAndNotValidState() throws Exception {
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/bookings?state={state}&from={from}&size={size}", "PPS", 0, 10)
                        .header("X-Sharer-User-Id", booker.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        Mockito.verify(userRepository, Mockito.times(1)).findById(booker.getId());
    }

    @Test
    @DisplayName("Send GET request /bookings/owner?state={state}&from={from}&size={size}")
    void findAllByItemOwnerId() throws Exception {
        MyPageRequest pageRequest = new MyPageRequest(0, 10, Sort.by(Sort.Direction.DESC, "start"));
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepository.findAllByItemOwnerId(owner.getId(), pageRequest)).thenReturn(List.of(booking));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/bookings/owner?state={state}&from={from}&size={size}", BookingState.PAST, 0, 10)
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").isNumber());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findAllByItemOwnerId(owner.getId(), pageRequest);
    }

    @Test
    @DisplayName("Send GET request /bookings/owner?state={state}&from={from}&size={size}")
    void findAllByItemOwnerIdAndNotValidState() throws Exception {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/bookings/owner?state={state}&from={from}&size={size}", "PPS", 0, 10)
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }

    @Test
    @DisplayName("Send POST request /bookings")
    void save() throws Exception {
        booking.setStart(booking.getStart().plusDays(15));
        booking.setEnd(booking.getEnd().plusDays(10));
        final BookingDto dto = BookingMapper.toBookingDto(booking);

        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.save(Mockito.any())).thenReturn(booking);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/bookings")
                        .header("X-Sharer-User-Id", booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(dto)))
                .andExpect(status().isOk());

        Mockito.verify(userRepository, Mockito.times(1)).findById(booker.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    @DisplayName("Send PATCH request /bookings/{id}?approved={approved}")
    void update() throws Exception {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/bookings/{id}?approved={approved}", booking.getId(), "false")
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(BookingStatus.REJECTED.name()));

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findById(booking.getId());
    }

    @Test
    @DisplayName("Send PATCH request /bookings/{id}?approved={approved}")
    void updateByNotValidStatus() throws Exception {
        booking.setStatus(BookingStatus.CANCELED);
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/bookings/{id}?approved={approved}", booking.getId(), "null")
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findById(booking.getId());
    }

    @Test
    @DisplayName("Send DELETE request /bookings/{id}")
    void deleteById() throws Exception {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/bookings/{id}", booking.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findById(booking.getId());
    }
}

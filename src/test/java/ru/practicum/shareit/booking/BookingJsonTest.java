package ru.practicum.shareit.booking;

import java.io.IOException;
import java.time.LocalDateTime;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.utils.LocalDateTimeAdapter;
import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingJsonTest {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .serializeNulls()
            .create();
    private static final Item ITEM = Item.builder()
            .id(1L)
            .name("Drill")
            .description("Drill 2000 MaxPro")
            .available(true)
            .build();
    private static final User BOOKER = new User(1L, "Nikolas", "nik@mail.ru");
    private static final Booking BOOKING = new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusDays(2),
            BookingStatus.WAITING, ITEM, BOOKER);
    @Autowired
    private JacksonTester<BookingDto> testerDto;
    @Autowired
    private JacksonTester<BookingInfoDto> testerInfoDto;

    @Test
    void bookingDtoSerialize() throws IOException {
        BookingDto dto = BookingMapper.toBookingDto(BOOKING);
        JsonContent<BookingDto> json = testerDto.write(dto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isNotNull();
        assertThat(json).extractingJsonPathStringValue("$.start").isNotNull();
        assertThat(json).extractingJsonPathStringValue("$.end").isNotNull();
        assertThat(json).extractingJsonPathNumberValue("$.itemId").isNotNull();
        assertThat(json).extractingJsonPathNumberValue("$.bookerId").isNotNull();
    }

    @Test
    void bookingInfoDtoSerialize() throws IOException {
        BookingInfoDto dto = BookingMapper.toBookingInfoDto(BOOKING);
        JsonContent<BookingInfoDto> json = testerInfoDto.write(dto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isNotNull();
        assertThat(json).extractingJsonPathStringValue("$.start").isNotNull();
        assertThat(json).extractingJsonPathStringValue("$.end").isNotNull();
        assertThat(json).extractingJsonPathStringValue("$.status").isEqualTo(dto.getStatus().name());
        assertThat(json).extractingJsonPathNumberValue("$.item.id").isNotNull();
        assertThat(json).extractingJsonPathNumberValue("$.booker.id").isNotNull();
    }

    @Test
    void bookingDtoDeserialize() throws IOException {
        BookingDto dto = BookingMapper.toBookingDto(BOOKING);
        String content = gson.toJson(dto);
        BookingDto result = testerDto.parse(content).getObject();

        assertThat(result.getId()).isEqualTo(dto.getId());
        assertThat(result.getStart()).isEqualTo(dto.getStart());
        assertThat(result.getEnd()).isEqualTo(dto.getEnd());
        assertThat(result.getStart()).isBefore(dto.getEnd());
        assertThat(result.getEnd()).isAfter(dto.getStart());
        assertThat(result.getItemId()).isEqualTo(dto.getItemId());
        assertThat(result.getBookerId()).isEqualTo(dto.getBookerId());
    }

    @Test
    void bookingInfoDtoDeserialize() throws IOException {
        BookingInfoDto dto = BookingMapper.toBookingInfoDto(BOOKING);
        String content = gson.toJson(dto);
        BookingInfoDto result = testerInfoDto.parse(content).getObject();

        assertThat(result.getId()).isEqualTo(dto.getId());
        assertThat(result.getStart()).isEqualTo(dto.getStart());
        assertThat(result.getEnd()).isEqualTo(dto.getEnd());
        assertThat(result.getStart()).isBefore(dto.getEnd());
        assertThat(result.getEnd()).isAfter(dto.getStart());
        assertThat(result.getStatus()).isEqualTo(dto.getStatus());
        assertThat(result.getItem()).isNotNull();
        assertThat(result.getBooker()).isNotNull();
    }
}

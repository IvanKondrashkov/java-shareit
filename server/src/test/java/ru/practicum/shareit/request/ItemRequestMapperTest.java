package ru.practicum.shareit.request;

import java.util.Set;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import static org.junit.jupiter.api.Assertions.*;

class ItemRequestMapperTest {
    private static final User REQUESTOR = new User(2L, "Bob", "bob@mail.ru");
    private static final ItemRequest REQUEST = new ItemRequest(
            1L, "Drill 2000 MaxPro", LocalDateTime.now(), REQUESTOR);
    private static final ItemRequestDto DTO = ItemRequestMapper.toItemRequestDto(REQUEST);

    @ParameterizedTest
    @MethodSource("getRequest")
    void toItemRequestDto(Set<Item> items) {
        ItemRequestDto dto = items == null ?
                ItemRequestMapper.toItemRequestDto(REQUEST) :
                ItemRequestMapper.toItemRequestDto(REQUEST, items);

        assertEquals(dto.getId(), REQUEST.getId());
        assertEquals(dto.getDescription(), REQUEST.getDescription());
        assertEquals(dto.getCreated(), REQUEST.getCreated());

        if (items == null) {
            assertNull(dto.getItems());
        } else {
            assertEquals(dto.getItems().size(), 0);
        }
    }

    @Test
    void toItemRequest() {
        ItemRequest request = ItemRequestMapper.toItemRequest(DTO, REQUESTOR);

        assertEquals(request.getId(), DTO.getId());
        assertEquals(request.getDescription(), DTO.getDescription());
        assertEquals(request.getRequestor(), REQUESTOR);
    }

    private static Stream<Arguments> getRequest() {
        return Stream.of(
                Arguments.of((Object) null),
                Arguments.of(Set.of())
        );
    }
}

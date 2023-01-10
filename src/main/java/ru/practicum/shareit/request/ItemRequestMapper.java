package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.Collections;
import java.util.Set;
import java.time.LocalDateTime;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import static java.util.stream.Collectors.toSet;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemRequestMapper {
    public static ItemRequestDto toItemRequestDto(ItemRequest request) {
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .build();
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest request, Set<Item> items) {
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(items == null ? Collections.emptySet() : items.stream()
                        .map(it -> ItemMapper.toItemDto(it, request))
                        .collect(toSet()))
                .build();
    }

    public static ItemRequest toItemRequest(ItemRequestDto requestDto, User requestor) {
        return new ItemRequest(
                requestDto.getId(),
                requestDto.getDescription(),
                LocalDateTime.now(),
                requestor
        );
    }
}

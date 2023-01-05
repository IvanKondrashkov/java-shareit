package ru.practicum.shareit.request;

import java.util.Set;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;

public class ItemRequestMapper {
    public static ItemRequestDto toItemRequestDto(@NotNull ItemRequest request) {
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .build();
    }

    public static ItemRequestDto toItemRequestDto(@NotNull ItemRequest request, @NotNull Set<Item> items) {
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(items.stream()
                        .map(it -> ItemMapper.toItemDto(it, request))
                        .collect(Collectors.toSet()))
                .build();
    }

    public static ItemRequest toItemRequest(@NotNull ItemRequestDto requestDto, @NotNull User requestor) {
        return new ItemRequest(
                requestDto.getId(),
                requestDto.getDescription(),
                LocalDateTime.now(),
                requestor
        );
    }
}

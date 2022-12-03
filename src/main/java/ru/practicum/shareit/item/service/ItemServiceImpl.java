package ru.practicum.shareit.item.service;

import java.util.List;
import java.util.stream.Collectors;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.dao.UserStorage;
import ru.practicum.shareit.item.dao.ItemStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemDto findById(Long userId, Long id) {
        final Item item = itemStorage.findById(userId, id);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> findByKeyWord(Long userId, String text) {
        return itemStorage.findByKeyWord(userId, text)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> findAll(Long userId) {
        return itemStorage.findAll(userId)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto save(ItemDto itemDto, Long userId) {
        final User owner = userStorage.findById(userId);
        final Item item = ItemMapper.toItem(itemDto, owner);
        itemStorage.save(item, userId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto update(ItemDto itemDto, Long userId, Long id) {
        final User owner = userStorage.findById(userId);
        final Item item = ItemMapper.toItem(itemDto, owner);
        itemStorage.update(item, userId, id);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public void deleteById(Long userId, Long id) {
        itemStorage.deleteById(userId, id);
    }
}

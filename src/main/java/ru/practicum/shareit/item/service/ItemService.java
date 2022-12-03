package ru.practicum.shareit.item.service;

import java.util.List;
import ru.practicum.shareit.item.dto.ItemDto;

public interface ItemService {
    /**
     * Find item dto by id.
     * @param userId User id, owner item.
     * @param id Item id.
     * @return ItemDto.
     */
    ItemDto findById(Long userId, Long id);

    /**
     * Find items dto by key word, flag available = true.
     * @param userId User id, owner item.
     * @param text Key word.
     * @return List items dto.
     */
    List<ItemDto> findByKeyWord(Long userId, String text);

    /**
     * Find all items dto by user id.
     * @param userId User id, owner item.
     * @return List items dto.
     */
    List<ItemDto> findAll(Long userId);

    /**
     * Create item dto.
     * @param itemDto Entity dto.
     * @param userId User id, owner item.
     * @return ItemDto.
     */
    ItemDto save(ItemDto itemDto, Long userId);

    /**
     * Update item dto by id. Only the owner can update it.
     * @param itemDto Entity dto.
     * @param userId User id, owner item.
     * @param id Item dto id.
     * @return ItemDto.
     */
    ItemDto update(ItemDto itemDto, Long userId, Long id);

    /**
     * Delete item dto by id. Only the owner can delete it.
     * @param userId User id, owner item.
     * @param id Item dto id.
     */
    void deleteById(Long userId, Long id);
}

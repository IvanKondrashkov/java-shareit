package ru.practicum.shareit.item.service;

import java.util.List;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentInfoDto;

public interface ItemService {
    /**
     * Find item by id.
     * @param userId User id, owner item.
     * @param id Item id.
     * @return ItemDto.
     */
    ItemDto findById(Long userId, Long id);

    /**
     * Find all items by key word, flag available = true.
     * @param userId User id, owner item.
     * @param text Key word.
     * @return List item dto.
     */
    List<ItemDto> findAllByText(Long userId, String text);

    /**
     * Find all items by user id.
     * @param userId User id, owner item.
     * @return List item dto.
     */
    List<ItemDto> findAll(Long userId);

    /**
     * Create item.
     * @param itemDto Entity dto.
     * @param userId User id, owner item.
     * @return ItemDto.
     */
    ItemDto save(ItemDto itemDto, Long userId);

    /**
     * Update item by id. Only the owner can update it.
     * @param itemDto Entity dto.
     * @param userId User id, owner item.
     * @param id Item id.
     * @return ItemDto.
     */
    ItemDto update(ItemDto itemDto, Long userId, Long id);

    /**
     * Delete item by id. Only the owner can delete it.
     * @param userId User id, owner item.
     * @param id Item id.
     */
    void deleteById(Long userId, Long id);

    /**
     * Add comment after booking item.
     * @param commentDto Entity dto.
     * @param userId User id.
     * @param id Comment id.
     * @return CommentInfoDto.
     */
    CommentInfoDto saveComment(CommentDto commentDto, Long userId, Long id);
}

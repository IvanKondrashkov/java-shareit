package ru.practicum.shareit.item.dao;

import java.util.List;
import ru.practicum.shareit.item.model.Item;

public interface ItemStorage {
    /**
     * Find item by id.
     * @param userId User id, owner item.
     * @param id Item id.
     * @return Item.
     */
    Item findById(Long userId, Long id);

    /**
     * Find item by key word, flag available = true.
     * @param userId User id, owner item.
     * @param text Key word.
     * @return List items.
     */
    List<Item> findByKeyWord(Long userId, String text);

    /**
     * Find all items by user id.
     * @param userId User id, owner item.
     * @return List item.
     */
    List<Item> findAll(Long userId);

    /**
     * Create item.
     * @param item Entity.
     * @param userId User id, owner item.
     * @return Item.
     */
    Item save(Item item, Long userId);

    /**
     * Update item by id. Only the owner can update it.
     * @param item Entity.
     * @param userId User id, owner item.
     * @param id Item id.
     * @return Item.
     */
    Item update(Item item, Long userId, Long id);

    /**
     * Delete item by id. Only the owner can delete it.
     * @param userId User id, owner item.
     * @param id Item id.
     */
    void deleteById(Long userId, Long id);
}

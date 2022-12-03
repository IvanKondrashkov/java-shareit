package ru.practicum.shareit.item.dao;

import java.util.*;
import java.util.stream.Collectors;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Item;
import java.util.concurrent.atomic.AtomicLong;
import ru.practicum.shareit.exception.UserConflictException;
import ru.practicum.shareit.exception.FieldEmptyException;
import ru.practicum.shareit.exception.FieldWithoutException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import org.springframework.stereotype.Repository;

@Repository
public class ItemStorageImpl implements ItemStorage {
    private AtomicLong idCurrent = new AtomicLong();
    private final Map<Long, Item> items = new HashMap<>();

    private Long getIdCurrent() {
        return idCurrent.incrementAndGet();
    }

    @Override
    public Item findById(Long userId, Long id) {
        if (!items.containsKey(id)) {
            throw new EntityNotFoundException(String.format("Item with id=%d not found!", id));
        }
        return items.get(id);
    }

    @Override
    public List<Item> findByKeyWord(Long userId, String text) {
        List<Item> listByKeyWords = items.values().stream()
                .filter(it -> it.getAvailable().equals(true))
                .filter(it -> it.getName().toLowerCase().contains(text.toLowerCase()) || it.getDescription().toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList());
        return text.isBlank() ? List.of() : listByKeyWords;
    }

    @Override
    public List<Item> findAll(Long userId) {
        if (userId == null) {
            throw new FieldWithoutException(String.format("Request header without userId=%d", userId));
        }
        return items.values().stream()
                .filter(it -> it.getOwner().getId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Item save(Item item, Long userId) {
        if (userId == null) {
            throw new FieldWithoutException(String.format("Request header without userId=%d",
                    userId)
            );
        }
        if (item.getAvailable() == null) {
            throw new FieldWithoutException(String.format("Item without available=%b",
                    item.getAvailable())
            );
        }
        if (item.getName().isBlank() || item.getDescription().isBlank()) {
            throw new FieldEmptyException(String.format("Item is empty fields name=%s or description=%s",
                    item.getName(),
                    item.getDescription())
            );
        }
        item.setId(getIdCurrent());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item, Long userId, Long id) {
        if (userId == null) {
            throw new FieldWithoutException(String.format("Request header without userId=%d",
                    userId)
            );
        }
        if (!items.containsKey(id)) {
            throw new EntityNotFoundException(String.format("Item with id=%d not found!",
                    id)
            );
        }
        final Item oldItem = items.get(id);
        final User owner = oldItem.getOwner();

        if (!owner.getId().equals(userId)) {
            throw new UserConflictException(String.format("User userId=%d is not the owner of the item!",
                    userId)
            );
        }
        item.setId(id);
        item.setName(item.getName() != null ? item.getName() : oldItem.getName());
        item.setDescription(item.getDescription() != null ? item.getDescription() : oldItem.getDescription());
        item.setAvailable(item.getAvailable() != null ? item.getAvailable() : oldItem.getAvailable());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public void deleteById(Long userId, Long id) {
        if (userId == null) {
            throw new FieldWithoutException(String.format("Request header without userId=%d",
                    userId)
            );
        }
        if (!items.containsKey(id)) {
            throw new EntityNotFoundException(String.format("Item with id=%d not found!",
                    id)
            );

        }
        final Item item = items.get(id);
        final User owner = item.getOwner();

        if (!owner.getId().equals(userId)) {
            throw new UserConflictException(String.format("User userId=%d is not the owner of the item!",
                    userId)
            );
        }
        items.remove(id);
    }
}

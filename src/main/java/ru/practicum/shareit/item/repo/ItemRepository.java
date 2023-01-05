package ru.practicum.shareit.item.repo;

import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Set;
import ru.practicum.shareit.item.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(Long userId);

    Set<Item> findAllByRequestId(Long requestId);

    @Query("select i from Item i where i.available = true and " +
            "(lower(i.name) like lower(concat('%', ?1, '%')) or " +
            "lower(i.description) like lower(concat('%', ?1, '%')))")
    List<Item> findAllByText(String text);
}

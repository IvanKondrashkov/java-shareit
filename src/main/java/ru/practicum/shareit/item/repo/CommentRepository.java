package ru.practicum.shareit.item.repo;

import java.util.Set;
import java.util.List;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Set<Comment> findAllByItemId(Long itemId);

    Set<Comment> findByItemIn(List<Item> items, Sort sort);
}

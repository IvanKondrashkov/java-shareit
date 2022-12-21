package ru.practicum.shareit.item.repo;

import java.util.Set;
import ru.practicum.shareit.item.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Set<Comment> findAllByItemId(Long itemId);
}

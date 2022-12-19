package ru.practicum.shareit.item.repo;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.util.Set;
import ru.practicum.shareit.item.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("select c from Comment c inner join Item i on c.item.id = ?1")
    Set<Comment> findByItemId(Long itemId);
}

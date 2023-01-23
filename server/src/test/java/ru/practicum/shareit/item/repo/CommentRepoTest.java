package ru.practicum.shareit.item.repo;

import java.util.List;
import java.util.Set;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.Comment;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class CommentRepoTest {
    private User owner;
    private User author;
    private Item item;
    private Comment comment;
    @Autowired
    private TestEntityManager em;
    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    void init() {
        owner = new User(null, "Nikolas", "nik@mail.ru");
        author = new User(null, "Djon", "djon@mail.ru");
        item = Item.builder()
                .id(null)
                .name("Drill")
                .description("Drill 2000 MaxPro")
                .available(true)
                .owner(owner)
                .build();
        comment = new Comment(null, "Very good Drill!", LocalDateTime.now(), item, author);

        em.persist(owner);
        em.persist(author);
        em.persist(item);
        em.persist(comment);
    }

    @AfterEach
    void tearDown() {
        owner = em.find(User.class, owner.getId());
        em.remove(owner);
        author = em.find(User.class, author.getId());
        em.remove(author);
        item = em.find(Item.class, item.getId());
        em.remove(item);
        comment = em.find(Comment.class, comment.getId());
        em.remove(comment);
        em.flush();
        em.clear();
    }

    @Test
    void findAllByItemId() {
        Set<Comment> comments = commentRepository.findAllByItemId(item.getId());

        assertNotNull(owner.getId());
        assertNotNull(author.getId());
        assertNotNull(item.getId());
        assertNotNull(comment.getId());
        assertEquals(1, comments.size());
    }

    @Test
    void findByItemIn() {
        Set<Comment> comments = commentRepository.findByItemIn(List.of(item), Sort.by(Sort.Direction.DESC, "created"));

        assertNotNull(owner.getId());
        assertNotNull(author.getId());
        assertNotNull(item.getId());
        assertNotNull(comment.getId());
        assertEquals(1, comments.size());
    }
}

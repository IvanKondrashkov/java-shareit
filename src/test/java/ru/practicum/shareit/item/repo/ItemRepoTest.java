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
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ItemRepoTest {
    private User owner;
    private User requestor;
    private ItemRequest request;
    private Item item;
    @Autowired
    private TestEntityManager em;
    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void init() {
        owner = new User(null, "Nikolas", "nik@mail.ru");
        requestor = new User(null, "Djon", "djon@mail.ru");
        request = new ItemRequest(null, "Drill 2000 MaxPro", LocalDateTime.now(), requestor);
        item = Item.builder()
                .id(null)
                .name("Drill")
                .description("Drill 2000 MaxPro")
                .available(true)
                .owner(owner)
                .request(request)
                .build();

        em.persist(owner);
        em.persist(requestor);
        em.persist(request);
        em.persist(item);
    }

    @AfterEach
    void tearDown() {
        owner = em.find(User.class, owner.getId());
        em.remove(owner);
        requestor = em.find(User.class, requestor.getId());
        em.remove(requestor);
        request = em.find(ItemRequest.class, request.getId());
        em.remove(request);
        item = em.find(Item.class, item.getId());
        em.remove(item);
        em.flush();
        em.clear();
    }

    @Test
    void findAllByOwnerId() {
        List<Item> items = itemRepository.findAllByOwnerId(owner.getId());

        assertNotNull(owner.getId());
        assertNotNull(requestor.getId());
        assertNotNull(request.getId());
        assertNotNull(item.getId());
        assertEquals(1, items.size());
    }

    @Test
    void findAllByRequestId() {
        Set<Item> items = itemRepository.findAllByRequestId(request.getId());

        assertNotNull(owner.getId());
        assertNotNull(requestor.getId());
        assertNotNull(request.getId());
        assertNotNull(item.getId());
        assertEquals(1, items.size());
    }

    @Test
    void findAllByText() {
        List<Item> items = itemRepository.findAllByText("Drill");

        assertNotNull(owner.getId());
        assertNotNull(requestor.getId());
        assertNotNull(request.getId());
        assertNotNull(item.getId());
        assertEquals(1, items.size());
    }
}

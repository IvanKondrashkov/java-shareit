package ru.practicum.shareit.item.model;

import lombok.*;
import javax.persistence.*;
import java.util.Set;
import java.util.HashSet;
import ru.practicum.shareit.user.model.User;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 100)
    private String name;
    @Column(length = 200)
    private String description;
    @Column(name = "is_available", nullable = false)
    private Boolean available;
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "comments", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "id")
    private final Set<Long> comments = new HashSet<>();
}

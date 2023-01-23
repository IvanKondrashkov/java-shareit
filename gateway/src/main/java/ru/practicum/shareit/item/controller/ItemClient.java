package ru.practicum.shareit.item.controller;

import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.client.BaseClient;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit.server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> findById(Long userId, Long id) {
        return get("/" + id, userId);
    }

    public ResponseEntity<Object> findAllByText(Long userId, String text) {
        Map<String, Object> parameters = Map.of(
                "text", text
        );
        return get("/search?text={text}", userId, parameters);
    }

    public ResponseEntity<Object> findAll(Long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> save(ItemDto dto, Long userId) {
        return post("", userId, dto);
    }

    public ResponseEntity<Object> saveComment(CommentDto dto, Long userId, Long id) {
        return post("/" + id + "/comment", userId, dto);
    }

    public ResponseEntity<Object> update(ItemDto dto, Long userId, Long id) {
        return patch("/" + id, userId, dto);
    }

    public ResponseEntity<Object> deleteById(Long userId, Long id) {
        return delete("/" + id, userId);
    }
}

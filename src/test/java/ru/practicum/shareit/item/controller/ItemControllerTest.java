package ru.practicum.shareit.item.controller;

import java.util.List;
import java.time.LocalDateTime;
import org.mockito.Mockito;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentInfoDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import javax.persistence.EntityNotFoundException;
import ru.practicum.shareit.exception.UserConflictException;
import ru.practicum.shareit.exception.CommentForbiddenException;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    private UserDto owner;
    private UserDto booker;
    private Item item;
    private ItemDto dto;
    private Comment comment;
    private CommentInfoDto commentInfoDto;
    private Gson gson;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemService itemService;

    @BeforeEach
    void init() {
        owner = new UserDto(1L, "Nikolas", "nik@mail.ru");
        booker = new UserDto(2L, "Djon", "djony@mail.ru");
        item = Item.builder()
                .id(1L)
                .name("Drill")
                .description("Cordless drill")
                .available(true)
                .owner(UserMapper.toUser(owner))
                .build();
        comment = new Comment(1L, "Good drill!", LocalDateTime.now(), item, UserMapper.toUser(booker));
        gson = new GsonBuilder()
                .serializeNulls()
                .create();
        dto = ItemMapper.toItemDto(item);
        commentInfoDto = CommentMapper.toCommentInfoDto(comment);
    }

    @AfterEach
    void tearDown() {
        owner = null;
        booker = null;
        item = null;
        comment = null;
        gson = null;
        dto = null;
        commentInfoDto = null;
    }

    @Test
    @DisplayName("Send GET request /items/{id}")
    void findById() throws Exception {
        Mockito.when(itemService.findById(owner.getId(), item.getId())).thenReturn(dto);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber());


        Mockito.verify(itemService, Mockito.times(1)).findById(owner.getId(), item.getId());
    }

    @Test
    @DisplayName("Send GET request /items/{id}")
    void findByNotValidId() throws Exception {
        Mockito.when(itemService.findById(owner.getId(), item.getId())).thenThrow(EntityNotFoundException.class);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(itemService, Mockito.times(1)).findById(owner.getId(), item.getId());
    }

    @Test
    @DisplayName("Send GET request /items/search?text={text}")
    void findAllByText() throws Exception {
        final String text = "Drill";
        Mockito.when(itemService.findAllByText(owner.getId(), text)).thenReturn(List.of(dto));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/items/search?text={text}", text)
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(itemService, Mockito.times(1)).findAllByText(owner.getId(), text);
    }

    @Test
    @DisplayName("Send GET request /items")
    void findAll() throws Exception {
        Mockito.when(itemService.findAll(owner.getId())).thenReturn(List.of(dto));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/items")
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").isNumber());

        Mockito.verify(itemService, Mockito.times(1)).findAll(owner.getId());
    }

    @Test
    @DisplayName("Send POST request /items")
    void save() throws Exception {
        dto = ItemMapper.toItemDto(item, new ItemRequest());
        Mockito.when(itemService.save(Mockito.any(), Mockito.anyLong())).thenReturn(dto);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/items")
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(dto)))
                .andExpect(status().isOk());

        Mockito.verify(itemService, Mockito.times(1)).save(Mockito.any(), Mockito.anyLong());
    }

    @Test
    @DisplayName("Send POST request /items/{id}/comment")
    void saveComment() throws Exception {
        CommentDto commentDto = CommentMapper.toCommentDto(comment);
        Mockito.when(itemService.saveComment(Mockito.any(), Mockito.anyLong(), Mockito.anyLong())).thenReturn(commentInfoDto);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/items/{id}/comment", item.getId())
                        .header("X-Sharer-User-Id", booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(commentDto)))
                .andExpect(status().isOk());

        Mockito.verify(itemService, Mockito.times(1)).saveComment(Mockito.any(), Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    @DisplayName("Send POST request /items/{id}/comment")
    void saveCommentByNotBooker() throws Exception {
        CommentDto commentDto = CommentMapper.toCommentDto(comment);
        Mockito.when(itemService.saveComment(Mockito.any(), Mockito.anyLong(), Mockito.anyLong())).thenThrow(CommentForbiddenException.class);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/items/{id}/comment", item.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(commentDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemService, Mockito.times(1)).saveComment(Mockito.any(), Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    @DisplayName("Send PATCH request /items/{id}")
    void update() throws Exception {
        dto.setName("Drill 2000");
        dto.setAvailable(false);
        dto.setDescription("Very good drill!");
        Mockito.when(itemService.update(Mockito.any(), Mockito.anyLong(), Mockito.anyLong())).thenReturn(dto);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(dto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Drill 2000"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.available").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Very good drill!"));

        Mockito.verify(itemService, Mockito.times(1)).update(Mockito.any(), Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    @DisplayName("Send PATCH request /items/{id}")
    void updateByNotOwner() throws Exception {
        Mockito.when(itemService.update(Mockito.any(), Mockito.anyLong(), Mockito.anyLong())).thenThrow(UserConflictException.class);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(dto)))
                .andExpect(status().isNotFound());

        Mockito.verify(itemService, Mockito.times(1)).update(Mockito.any(), Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    @DisplayName("Send DELETE request items/{id}")
    void deleteById() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(itemService, Mockito.times(1)).deleteById(owner.getId(), item.getId());
    }
}

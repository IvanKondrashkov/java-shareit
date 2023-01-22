package ru.practicum.shareit.request.service;

import java.util.List;
import ru.practicum.shareit.request.dto.ItemRequestDto;

public interface ItemRequestService {
    /**
     * Find item request by id.
     * @param userId User id.
     * @param id Item request id.
     * @return ItemRequestDto.
     */
    ItemRequestDto findById(Long userId, Long id);

    /**
     * Find all item request.
     * @param userId User id.
     * @return List item request dto.
     */
    List<ItemRequestDto> findAll(Long userId);

    /**
     * Find all item request, by page.
     * @param userId User id.
     * @param from Initial element.
     * @param size Page size.
     * @return List item request dto.
     */
    List<ItemRequestDto> findByPage(Long userId, Integer from, Integer size);

    /**
     * Create item request.
     * @param itemRequestDto Entity dto.
     * @param userId User id.
     * @return ItemRequestDto.
     */
    ItemRequestDto save(ItemRequestDto itemRequestDto, Long userId);

    /**
     * Delete item request by id.
     * @param userId User id.
     * @param id ItemRequest id.
     */
    void deleteById(Long userId, Long id);
}

package ru.practicum.shareit.request.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.MyPageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repo.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;
import javax.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository requestRepository;

    @Override
    public ItemRequestDto findById(Long userId, Long id) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final ItemRequest requestWrap = requestRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Item request with id=%d not found!", id))
        );
        final Set<Item> items = itemRepository.findAllByRequestId(requestWrap.getId());
        return ItemRequestMapper.toItemRequestDto(requestWrap, items);
    }

    @Override
    public List<ItemRequestDto> findAll(Long userId) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        return requestRepository.findAllByRequestorId(userWrap.getId()).stream()
                .map(it -> ItemRequestMapper.toItemRequestDto(it, itemRepository.findAllByRequestId(it.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> findByPage(Long userId, Integer from, Integer size) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final MyPageRequest pageRequest = new MyPageRequest(from, size, Sort.by(Sort.Direction.DESC, "created"));
        return requestRepository.findAllByRequestorIdNot(userWrap.getId(), pageRequest).stream()
                .map(it -> ItemRequestMapper.toItemRequestDto(it, itemRepository.findAllByRequestId(it.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemRequestDto save(ItemRequestDto requestDto, Long userId) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final ItemRequest request = ItemRequestMapper.toItemRequest(requestDto, userWrap);
        final ItemRequest requestWrap = requestRepository.save(request);
        return ItemRequestMapper.toItemRequestDto(requestWrap);
    }

    @Override
    @Transactional
    public void deleteById(Long userId, Long id) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final ItemRequest requestWrap = requestRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Item request with id=%d not found!", id))
        );
        requestRepository.deleteById(requestWrap.getId());
    }
}

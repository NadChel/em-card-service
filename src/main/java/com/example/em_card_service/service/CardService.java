package com.example.em_card_service.service;

import com.example.em_card_service.data.CardStatus;
import com.example.em_card_service.data.dto.request.CardRequestDto;
import com.example.em_card_service.data.dto.request.TransferRequestDto;
import com.example.em_card_service.data.dto.response.CardBalanceResponseDto;
import com.example.em_card_service.data.dto.response.CardResponseDto;
import com.example.em_card_service.data.entity.Card;
import com.example.em_card_service.data.entity.CardBlockRequest;
import com.example.em_card_service.data.entity.User;
import com.example.em_card_service.exception.TransferException;
import com.example.em_card_service.repository.CardBlockRequestRepository;
import com.example.em_card_service.repository.CardRepository;
import com.example.em_card_service.repository.UserRepository;
import com.example.em_card_service.mapper.CardMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardBlockRequestRepository blockRequestRepository;
    private final CardMapper mapper;
    private final TextEncryptor encryptor;

    public List<CardResponseDto> findAll(Pageable pageable) {
        List<Card> cards = cardRepository.findAll(pageable);
        return cards.stream()
                .map(mapper::toDto)
                .map(this::decryptAndMask)
                .toList();
    }

    private CardResponseDto decryptAndMask(CardResponseDto cardResponseDto) {
        String decryptedNumber = encryptor.decrypt(cardResponseDto.getNumber());
        String maskedNumber = "**** **** **** " + StringUtils.right(decryptedNumber, 4);
        cardResponseDto.setNumber(maskedNumber);
        return cardResponseDto;
    }

    public List<CardResponseDto> findByOwnerId(UUID userId, Pageable pageable) {
        List<Card> cards = cardRepository.findByOwnerId(userId, pageable);
        return cards.stream()
                .map(mapper::toDto)
                .map(this::decryptAndMask)
                .toList();
    }

    public CardBalanceResponseDto findBalance(UUID id, UUID userId) {
        Card card = loadCard(id);
        validateOwnership(userId, card);
        return mapper.toBalanceDto(card);
    }

    private Card loadCard(UUID id) {
        return cardRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Card not found"));
    }

    private static void validateOwnership(UUID userId, Card... cards) {
        boolean owned = Arrays.stream(cards).map(Card::getOwner).map(User::getId).allMatch(Predicate.isEqual(userId));
        if (!owned) throw new AccessDeniedException("No ownership of one or more involved cards");
    }

    @Transactional(readOnly = false)
    public CardResponseDto save(CardRequestDto cardRequestDto) {
        Card card = mapper.toCard(cardRequestDto);
        card.setOwner(loadUser(cardRequestDto.getOwnerId()));
        card.setNumber(encryptor.encrypt(normalizeNumber(cardRequestDto.getNumber())));
        Card savedCard = cardRepository.save(card);
        CardResponseDto cardResponseDto = mapper.toDto(savedCard);
        return decryptAndMask(cardResponseDto);
    }

    private User loadUser(UUID ownerId) {
        return userRepository.findById(ownerId).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private String normalizeNumber(String number) {
        return number.replaceAll("[\\s-]", "");
    }

    @Transactional(readOnly = false)
    public CardResponseDto updateStatus(UUID id, CardStatus newStatus) {
        Card card = loadCard(id);
        card.setStatus(newStatus);
        return mapper.toDto(card);
    }

    @Transactional(readOnly = false)
    public CardResponseDto transfer(TransferRequestDto transferDto, UUID userId) {
        Card originatingCard = loadCard(transferDto.getOriginatingCardId());
        Card recipientCard = loadCard(transferDto.getRecipientCardId());
        BigDecimal amount = transferDto.getAmount();
        validateTransfer(userId, originatingCard, recipientCard, amount);

        originatingCard.setBalance(originatingCard.getBalance().subtract(amount));
        recipientCard.setBalance(recipientCard.getBalance().add(amount));
        CardResponseDto originatingCardDto = mapper.toDto(originatingCard);
        return decryptAndMask(originatingCardDto);
    }

    private static void validateTransfer(UUID userId, Card originatingCard, Card recipientCard, BigDecimal amount) {
        validateOwnership(userId, originatingCard, recipientCard);
        if (originatingCard.getId().equals(recipientCard.getId())) throw new TransferException("Cannot perform transfer to the same card");
        if (originatingCard.getBalance().compareTo(amount) < 0) throw new TransferException("Insufficient balance");
    }

    @Transactional(readOnly = false)
    public CardResponseDto deposit(UUID id, BigDecimal amount, UUID userId) {
        Card card = loadCard(id);
        validateOwnership(userId, card);

        card.setBalance(card.getBalance().add(amount));
        CardResponseDto targetCardDto = mapper.toDto(card);
        return decryptAndMask(targetCardDto);
    }

    @Transactional(readOnly = false)
    public void requestBlock(UUID id, UUID userId) {
        Card card = loadCard(id);
        validateOwnership(userId, card);

        CardBlockRequest request = CardBlockRequest.from(card);
        blockRequestRepository.save(request);
    }

    @Transactional(readOnly = false)
    public void deleteById(UUID id) {
        cardRepository.deleteById(id);
    }
}

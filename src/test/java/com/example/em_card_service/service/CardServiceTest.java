package com.example.em_card_service.service;

import com.example.em_card_service.data.CardStatus;
import com.example.em_card_service.data.RequestStatus;
import com.example.em_card_service.data.dto.request.CardRequestDto;
import com.example.em_card_service.data.dto.request.TransferRequestDto;
import com.example.em_card_service.data.dto.response.CardBalanceResponseDto;
import com.example.em_card_service.data.dto.response.CardResponseDto;
import com.example.em_card_service.data.entity.Card;
import com.example.em_card_service.data.entity.CardBlockRequest;
import com.example.em_card_service.data.entity.User;
import com.example.em_card_service.mapper.CardMapper;
import com.example.em_card_service.repository.CardBlockRequestRepository;
import com.example.em_card_service.repository.CardRepository;
import com.example.em_card_service.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    CardRepository cardRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    CardBlockRequestRepository blockRequestRepository;
    @Mock
    CardMapper mapper;
    @Mock
    TextEncryptor encryptor;
    @InjectMocks
    CardService cardService;

    @Test
    void findAll_givenNoCardsExist_returnsEmptyList() {
        given(cardRepository.findAll(any())).willReturn(Collections.emptyList());

        List<CardResponseDto> cards = cardService.findAll(PageRequest.ofSize(1));

        assertThat(cards).isEmpty();
        then(mapper).shouldHaveNoInteractions();
        then(encryptor).shouldHaveNoInteractions();
    }

    @Test
    void findAll_givenCardsExist_returnsExpectedDtos() {
        Card cardOne = Card.builder().number("aaaa aaaa aaaa aaaa").build();
        Card cardTwo = Card.builder().number("bbbb bbbb bbbb bbbb").build();
        List<Card> cards = List.of(cardOne, cardTwo);

        Pageable pageable = PageRequest.ofSize(2);
        given(cardRepository.findAll(pageable)).willReturn(cards);

        given(encryptor.decrypt(cardOne.getNumber())).willReturn("4134 7702 0190 4496");
        given(encryptor.decrypt(cardTwo.getNumber())).willReturn("4396 5433 6321 3037");

        CardResponseDto expectedResponseCardOne = CardResponseDto.builder().number(encryptor.decrypt(cardOne.getNumber())).build();
        CardResponseDto expectedResponseCardTwo = CardResponseDto.builder().number(encryptor.decrypt(cardTwo.getNumber())).build();

        given(mapper.toDto(cardOne)).willReturn(expectedResponseCardOne);
        given(mapper.toDto(cardTwo)).willReturn(expectedResponseCardTwo);

        List<CardResponseDto> responseCards = cardService.findAll(pageable);
        assertThat(responseCards).hasSize(2);
        CardResponseDto responseCardOne = responseCards.get(0);
        CardResponseDto responseCardTwo = responseCards.get(1);
        assertThat(responseCardOne).extracting(CardResponseDto::getNumber).isEqualTo("**** **** **** " + StringUtils.right(expectedResponseCardOne.getNumber(), 4));
        assertThat(responseCardTwo).extracting(CardResponseDto::getNumber).isEqualTo("**** **** **** " + StringUtils.right(expectedResponseCardTwo.getNumber(), 4));
    }

    @Test
    void findByOwnerId_givenCardsExist_returnsExpectedDtos() {
        UUID userId = UUID.randomUUID();
        Card cardOne = Card.builder().number("aaaa aaaa aaaa aaaa").build();
        Card cardTwo = Card.builder().number("bbbb bbbb bbbb bbbb").build();
        List<Card> cards = List.of(cardOne, cardTwo);

        Pageable pageable = PageRequest.ofSize(2);
        given(cardRepository.findByOwnerId(userId, pageable)).willReturn(cards);

        given(encryptor.decrypt(cardOne.getNumber())).willReturn("4134 7702 0190 4496");
        given(encryptor.decrypt(cardTwo.getNumber())).willReturn("4396 5433 6321 3037");

        CardResponseDto expectedResponseCardOne = CardResponseDto.builder().number(encryptor.decrypt(cardOne.getNumber())).build();
        CardResponseDto expectedResponseCardTwo = CardResponseDto.builder().number(encryptor.decrypt(cardTwo.getNumber())).build();

        given(mapper.toDto(cardOne)).willReturn(expectedResponseCardOne);
        given(mapper.toDto(cardTwo)).willReturn(expectedResponseCardTwo);

        List<CardResponseDto> responseCards = cardService.findByOwnerId(userId, pageable);
        assertThat(responseCards).hasSize(2);
        CardResponseDto responseCardOne = responseCards.get(0);
        CardResponseDto responseCardTwo = responseCards.get(1);
        assertThat(responseCardOne).extracting(CardResponseDto::getNumber).isEqualTo("**** **** **** " + StringUtils.right(expectedResponseCardOne.getNumber(), 4));
        assertThat(responseCardTwo).extracting(CardResponseDto::getNumber).isEqualTo("**** **** **** " + StringUtils.right(expectedResponseCardTwo.getNumber(), 4));
    }

    @Test
    void findBalance_givenCardExists_butUserHasNoOwnership_returnsExpectedDto() {
        UUID cardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal balance = BigDecimal.TEN;
        User otherOwner = User.builder().id(UUID.randomUUID()).build();
        assumeThat(otherOwner.getId()).isNotEqualTo(userId);
        Card card = Card.builder().balance(balance).owner(otherOwner).build();
        given(cardRepository.findById(cardId)).willReturn(Optional.ofNullable(card));

        assertThatThrownBy(() -> cardService.findBalance(cardId, userId)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void findBalance_givenCardExists_userHasOwnership_returnsExpectedDto() {
        UUID cardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal balance = BigDecimal.TEN;
        User owner = User.builder().id(userId).build();
        Card card = Card.builder().balance(balance).owner(owner).build();
        given(cardRepository.findById(cardId)).willReturn(Optional.ofNullable(card));
        given(mapper.toBalanceDto(card)).willReturn(CardBalanceResponseDto.forBalance(balance));

        CardBalanceResponseDto balanceDto = cardService.findBalance(cardId, userId);

        assertThat(balanceDto.getBalance()).isEqualTo(balance);
    }

    @Test
    void save_givenCardSaved_returnsExpectedDto() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        String number = "4823 3854 1203 1652";
        YearMonth expirationDate = YearMonth.now().plusYears(1);
        CardRequestDto cardRequestDto = CardRequestDto.builder()
                .number(number)
                .ownerId(userId)
                .expirationDate(expirationDate)
                .build();
        Card card = Card.builder()
                .number(number)
                .owner(user)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .expirationDate(expirationDate)
                .build();
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(cardRepository.save(same(card))).willReturn(card);
        given(mapper.toCard(cardRequestDto)).willReturn(card);
        given(mapper.toDto(card)).willAnswer(i -> CardResponseDto.builder()
                .id(i.getArgument(0, Card.class).getId())
                .number(i.getArgument(0, Card.class).getNumber())
                .ownerId(i.getArgument(0, Card.class).getOwner().getId())
                .expirationDate(i.getArgument(0, Card.class).getExpirationDate())
                .status(i.getArgument(0, Card.class).getStatus())
                .balance(i.getArgument(0, Card.class).getBalance())
                .build());
        given(encryptor.encrypt(any())).willAnswer(i -> i.getArgument(0));
        given(encryptor.decrypt(any())).willAnswer(i -> i.getArgument(0));

        CardResponseDto responseCard = cardService.save(cardRequestDto);

        assertThat(responseCard.getOwnerId()).isEqualTo(userId);
        assertThat(responseCard.getExpirationDate()).isEqualTo(expirationDate);
        assertThat(responseCard.getStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(responseCard.getBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(responseCard.getNumber()).isEqualTo("**** **** **** " + StringUtils.right(number, 4));
        then(cardRepository).should().save(card);
    }

    @Test
    void updateStatus_givenStatusUpdated_returnsExpectedDto() {
        UUID cardId = UUID.randomUUID();
        Card card = Card.builder().id(cardId).build();
        CardStatus newStatus = CardStatus.BLOCKED;
        CardResponseDto expectedResponseCard = CardResponseDto.builder().id(cardId).status(newStatus).build();
        given(cardRepository.findById(cardId)).willReturn(Optional.ofNullable(card));
        given(mapper.toDto(card)).willReturn(expectedResponseCard);

        CardResponseDto responseCard = cardService.updateStatus(cardId, newStatus);

        assertThat(responseCard).isEqualTo(expectedResponseCard);
        assertThat(card.getStatus()).isEqualTo(newStatus);
    }

    @Test
    void transfer_givenCardsExists_userHasOwnership_transfersSpecifiedAmount() {
        UUID originatingCardId = UUID.randomUUID();
        UUID recipientCardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.TEN;
        BigDecimal originatingInitialBalance = BigDecimal.TEN;
        BigDecimal recipientInitialBalance = BigDecimal.ZERO;
        User user = User.builder().id(userId).build();
        Card originatingCard = Card.builder().id(originatingCardId).balance(originatingInitialBalance).owner(user).build();
        Card recipientCard = Card.builder().id(recipientCardId).balance(recipientInitialBalance).owner(user).build();
        given(cardRepository.findById(originatingCardId)).willReturn(Optional.ofNullable(originatingCard));
        given(cardRepository.findById(recipientCardId)).willReturn(Optional.ofNullable(recipientCard));

        given(mapper.toDto(any())).willAnswer(i -> CardResponseDto.builder().balance(i.getArgument(0, Card.class).getBalance()).build());

        TransferRequestDto transferDto = TransferRequestDto.builder()
                .originatingCardId(originatingCardId)
                .recipientCardId(recipientCardId)
                .amount(amount)
                .build();
        CardResponseDto originatinResponseCard = cardService.transfer(transferDto, userId);
        assertThat(originatinResponseCard.getBalance()).isEqualTo(originatingInitialBalance.subtract(amount));
        assertThat(originatingCard.getBalance()).isEqualTo(originatinResponseCard.getBalance());
        assertThat(recipientCard.getBalance()).isEqualTo(recipientInitialBalance.add(amount));
    }

    @Test
    void deposit_givenCardExists_increasesBalanceBySpecifiedAmount() {
        UUID cardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.TEN;
        BigDecimal initialBalance = BigDecimal.TEN;
        User user = User.builder().id(userId).build();
        Card card = Card.builder().balance(initialBalance).owner(user).build();
        given(cardRepository.findById(cardId)).willReturn(Optional.ofNullable(card));

        given(mapper.toDto(card)).willAnswer(i -> CardResponseDto.builder().balance(i.getArgument(0, Card.class).getBalance()).build());

        CardResponseDto responseCard = cardService.deposit(cardId, amount, userId);
        assertThat(responseCard.getBalance()).isEqualTo(amount.add(initialBalance));
        assertThat(card.getBalance()).isEqualTo(responseCard.getBalance());
    }

    @Test
    void requestCardBlock_givenCardExists_requestsCardBlock() {
        UUID cardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User owner = User.builder().id(userId).build();
        Card card = Card.builder().id(cardId).owner(owner).build();
        given(cardRepository.findById(cardId)).willReturn(Optional.of(card));

        cardService.requestBlock(cardId, userId);

        ArgumentCaptor<CardBlockRequest> captor = ArgumentCaptor.forClass(CardBlockRequest.class);
        then(blockRequestRepository).should().save(captor.capture());
        CardBlockRequest savedRequest = captor.getValue();
        assertThat(savedRequest.getStatus()).isEqualTo(RequestStatus.PENDING);
        Card savedRequestCard = savedRequest.getCard();
        assertThat(savedRequestCard.getId()).isEqualTo(cardId);
    }

    @Test
    void deleteById_givenCardExists_cardDeleted() {
        UUID cardId = UUID.randomUUID();
        willDoNothing().given(cardRepository).deleteById(cardId);

        cardService.deleteById(cardId);

        then(cardRepository).should().deleteById(cardId);
    }
}
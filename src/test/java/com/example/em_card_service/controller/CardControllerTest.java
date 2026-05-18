package com.example.em_card_service.controller;

import com.example.em_card_service.data.CardStatus;
import com.example.em_card_service.data.dto.request.CardRequestDto;
import com.example.em_card_service.data.dto.request.CardStatusRequestDto;
import com.example.em_card_service.data.dto.request.DepositRequestDto;
import com.example.em_card_service.data.dto.request.TransferRequestDto;
import com.example.em_card_service.data.dto.response.CardBalanceResponseDto;
import com.example.em_card_service.data.dto.response.CardResponseDto;
import com.example.em_card_service.service.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;

@WebMvcTest(CardController.class)
class CardControllerTest {

    @Autowired
    MockMvcTester mvcTester;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    CardService cardService;

    @Test
    void findAll_givenCardsPresent_returns200_andExpectedDtos() {
        UUID cardId = UUID.randomUUID();
        String number = "**** **** **** 1234";
        CardResponseDto card = CardResponseDto.builder().id(cardId).number(number).build();
        List<CardResponseDto> cards = List.of(card);
        given(cardService.findAll(any())).willReturn(cards);

        mvcTester.get().uri("/api/admin/cards")
                .with(adminJwt())
                .exchange()
                .assertThat()
                .hasStatusOk()
                .bodyJson().convertTo(list(CardResponseDto.class))
                .hasSize(1)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyElementsOf(cards);
    }

    private static JwtRequestPostProcessor adminJwt() {
        return jwt(UUID.randomUUID(), "ADMIN");
    }

    private static JwtRequestPostProcessor jwt(UUID userId) {
        return jwt(userId, "USER");
    }

    private static JwtRequestPostProcessor jwt(UUID userId, String role) {
        return SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt
                .subject(userId.toString())
                .claim("roles", List.of(role)));
    }

    @Test
    void findByUserId_givenCardsPresent_returns200_andExpectedDtos() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        String number = "**** **** **** 1234";
        CardResponseDto card = CardResponseDto.builder().id(cardId).number(number).build();
        List<CardResponseDto> cards = List.of(card);
        given(cardService.findByOwnerId(eq(userId), any())).willReturn(cards);

        mvcTester.get().uri("/api/cards")
                .with(jwt(userId))
                .exchange()
                .assertThat()
                .hasStatusOk()
                .bodyJson().convertTo(list(CardResponseDto.class))
                .hasSize(1)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyElementsOf(cards);
    }

    @Test
    void findBalance_givenCardPresent_returns200_andExpectedDto() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        CardBalanceResponseDto balanceDto = CardBalanceResponseDto.forBalance(BigDecimal.TEN);
        given(cardService.findBalance(cardId, userId)).willReturn(balanceDto);

        mvcTester.get().uri("/api/cards/{id}/balance", cardId)
                .with(jwt(userId))
                .exchange()
                .assertThat()
                .hasStatusOk()
                .bodyJson().convertTo(CardBalanceResponseDto.class)
                .usingRecursiveComparison()
                .isEqualTo(balanceDto);
    }

    @Test
    void save_givenCardSaved_returns201_andExpectedDto() {
        String number = "4089 3555 5396 5397";
        YearMonth expirationDate = YearMonth.now().plusYears(1);
        CardRequestDto cardRequestDto = CardRequestDto.builder()
                .number(number)
                .expirationDate(expirationDate)
                .build();
        CardResponseDto cardResponseDto = CardResponseDto.builder()
                .number("**** **** **** 1945")
                .expirationDate(expirationDate)
                .build();
        given(cardService.save(cardRequestDto)).willReturn(cardResponseDto);

        mvcTester.post().uri("/api/admin/cards")
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardRequestDto))
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .bodyJson().convertTo(CardResponseDto.class)
                .usingRecursiveComparison()
                .isEqualTo(cardResponseDto);
    }

    @Test
    void transfer_givenCardSaved_returns200_andExpectedDto() {
        UUID origCardId = UUID.randomUUID();
        UUID recipCardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TransferRequestDto transferRequestDto = TransferRequestDto.builder()
                .originatingCardId(origCardId)
                .originatingCardId(recipCardId)
                .amount(BigDecimal.TEN)
                .build();
        CardResponseDto cardResponseDto = CardResponseDto.builder()
                .number("**** **** **** 1945")
                .balance(BigDecimal.ZERO)
                .build();
        given(cardService.transfer(transferRequestDto, userId)).willReturn(cardResponseDto);

        mvcTester.post().uri("/api/cards/transfer")
                .with(jwt(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequestDto))
                .exchange()
                .assertThat()
                .hasStatusOk()
                .bodyJson().convertTo(CardResponseDto.class)
                .isEqualTo(cardResponseDto);
    }

    @Test
    void deposit_givenSuccessfulDeposit_returns200_andExpectedDto() {
        UUID cardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.TEN;
        DepositRequestDto depositDto = DepositRequestDto.forAmount(amount);
        CardResponseDto cardResponseDto = CardResponseDto.builder()
                .number("**** **** **** 1945")
                .balance(BigDecimal.TEN)
                .build();
        given(cardService.deposit(cardId, amount, userId)).willReturn(cardResponseDto);

        mvcTester.post().uri("/api/cards/{id}/deposit", cardId)
                .with(jwt(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositDto))
                .exchange()
                .assertThat()
                .hasStatusOk()
                .bodyJson().convertTo(CardResponseDto.class)
                .isEqualTo(cardResponseDto);
    }

    @Test
    void requestBlock_givenSuccessfulRequest_returns200() {
        UUID cardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        willDoNothing().given(cardService).requestBlock(cardId, userId);

        mvcTester.post().uri("/api/cards/{id}/block", cardId)
                .with(jwt(userId))
                .exchange()
                .assertThat()
                .hasStatusOk()
                .body().isEmpty();
    }

    @Test
    void updateStatus_givenStatusUpdated_returnsExpectedDto() {
        UUID cardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CardResponseDto cardResponseDto = CardResponseDto.builder()
                .id(cardId)
                .ownerId(userId)
                .status(CardStatus.BLOCKED)
                .build();
        CardStatusRequestDto statusDto = CardStatusRequestDto.forStatus(CardStatus.BLOCKED);
        given(cardService.updateStatus(cardId, CardStatus.BLOCKED)).willReturn(cardResponseDto);

        mvcTester.patch().uri("/api/admin/cards/{id}", cardId)
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusDto))
                .exchange()
                .assertThat()
                .hasStatusOk()
                .bodyJson().convertTo(CardResponseDto.class)
                .isEqualTo(cardResponseDto);
    }

    @Test
    void deleteById_givenCardDeleted_returns200() {
        UUID cardId = UUID.randomUUID();
        willDoNothing().given(cardService).deleteById(cardId);

        mvcTester.delete().uri("/api/admin/cards/{id}", cardId)
                .with(adminJwt())
                .exchange()
                .assertThat()
                .hasStatusOk()
                .body().isEmpty();
    }
}
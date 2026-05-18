package com.example.em_card_service.controller;

import com.example.em_card_service.data.dto.request.CardRequestDto;
import com.example.em_card_service.data.dto.request.CardStatusRequestDto;
import com.example.em_card_service.data.dto.request.DepositRequestDto;
import com.example.em_card_service.data.dto.request.TransferRequestDto;
import com.example.em_card_service.data.dto.response.CardBalanceResponseDto;
import com.example.em_card_service.data.dto.response.CardResponseDto;
import com.example.em_card_service.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@SecurityScheme(type = SecuritySchemeType.HTTP,
        name = "bearer-key",
        scheme = "bearer", bearerFormat = "JWT")
public class CardController {

    private final CardService service;

    @GetMapping("/admin/cards")
    public ResponseEntity<List<CardResponseDto>> findAll(@ParameterObject @PageableDefault Pageable pageable) {
        List<CardResponseDto> cards = service.findAll(pageable);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/cards")
    public ResponseEntity<List<CardResponseDto>> findByUserId(@ParameterObject @PageableDefault Pageable pageable,
                                                              @AuthenticationPrincipal JwtClaimAccessor claimAccessor) {
        UUID userId = UUID.fromString(claimAccessor.getSubject());
        List<CardResponseDto> cards = service.findByOwnerId(userId, pageable);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/cards/{id}/balance")
    public ResponseEntity<CardBalanceResponseDto> findBalance(@PathVariable UUID id,
                                                              @AuthenticationPrincipal JwtClaimAccessor claimAccessor) {
        UUID userId = UUID.fromString(claimAccessor.getSubject());
        CardBalanceResponseDto card = service.findBalance(id, userId);
        return ResponseEntity.ok(card);
    }

    @PostMapping("/admin/cards")
    public ResponseEntity<CardResponseDto> save(@RequestBody @Valid CardRequestDto cardRequestDto) {
        CardResponseDto savedUserResponseDto = service.save(cardRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUserResponseDto);
    }

    @PostMapping("/cards/transfer")
    @ApiResponse(responseCode = "200", description = "The originating (funding) card.")
    public ResponseEntity<CardResponseDto> transfer(@RequestBody @Valid TransferRequestDto transferDto,
                                                    @AuthenticationPrincipal JwtClaimAccessor claimAccessor) {
        UUID userId = UUID.fromString(claimAccessor.getSubject());
        CardResponseDto updatedCardDto = service.transfer(transferDto, userId);
        return ResponseEntity.ok(updatedCardDto);
    }

    @PostMapping("/cards/{id}/deposit")
    @ApiResponse(responseCode = "200", description = "The topped-up card.")
    public ResponseEntity<CardResponseDto> deposit(@PathVariable UUID id,
                                                   @RequestBody @Valid DepositRequestDto depositDto,
                                                   @AuthenticationPrincipal JwtClaimAccessor claimAccessor) {
        UUID userId = UUID.fromString(claimAccessor.getSubject());
        CardResponseDto updatedCardDto = service.deposit(id, depositDto.getAmount(), userId);
        return ResponseEntity.ok(updatedCardDto);
    }

    @PostMapping("/cards/{id}/block")
    public ResponseEntity<Void> requestBlock(@PathVariable UUID id,
                                             @AuthenticationPrincipal JwtClaimAccessor claimAccessor) {
        UUID userId = UUID.fromString(claimAccessor.getSubject());
        service.requestBlock(id, userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/admin/cards/{id}")
    @Operation(description = "Updates the card's status. Supported statuses: ACTIVE, BLOCKED")
    @ApiResponse(responseCode = "200", description = "The updated card.")
    public ResponseEntity<CardResponseDto> updateStatus(@PathVariable UUID id,
                                                        @RequestBody CardStatusRequestDto statusDto) {
        CardResponseDto updatedCardDto = service.updateStatus(id, statusDto.getStatus());
        return ResponseEntity.ok(updatedCardDto);
    }

    @DeleteMapping("/admin/cards/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        service.deleteById(id);
        return ResponseEntity.ok().build();
    }
}

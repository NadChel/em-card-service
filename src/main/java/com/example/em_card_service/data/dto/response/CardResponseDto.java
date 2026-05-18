package com.example.em_card_service.data.dto.response;

import com.example.em_card_service.data.CardStatus;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonPropertyOrder({"id", "status", "number", "expirationDate", "balance", "ownerId"})
public class CardResponseDto {

    private UUID id;
    @Schema(example = "**** **** **** 1652", description = "Masked card number")
    private String number;
    private UUID ownerId;
    @Schema(example = "2100-01")
    private YearMonth expirationDate;
    private CardStatus status;
    @Schema(example = "10")
    private BigDecimal balance;
}

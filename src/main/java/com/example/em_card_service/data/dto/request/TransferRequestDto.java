package com.example.em_card_service.data.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TransferRequestDto {

    private UUID originatingCardId;
    private UUID recipientCardId;
    @Schema(example = "10")
    @Positive
    @Digits(integer = Integer.MAX_VALUE, fraction = 2)
    private BigDecimal amount;
}

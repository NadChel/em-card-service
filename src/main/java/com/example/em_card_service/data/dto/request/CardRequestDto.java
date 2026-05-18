package com.example.em_card_service.data.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.CreditCardNumber;

import java.time.YearMonth;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class CardRequestDto {

    @Schema(example = "4823 3854 1203 1652",
            description = "Card number. Spaces or hyphens to separate digit groups are allowed (but not required)")
    @CreditCardNumber(ignoreNonDigitCharacters = true)
    private String number;
    @Future
    @Schema(example = "2100-01")
    private YearMonth expirationDate;
    private UUID ownerId;
}

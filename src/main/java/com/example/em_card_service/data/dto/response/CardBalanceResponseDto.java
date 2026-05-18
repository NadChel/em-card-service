package com.example.em_card_service.data.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@EqualsAndHashCode
public class CardBalanceResponseDto {

    @Schema(example = "10")
    private BigDecimal balance;

    public static CardBalanceResponseDto forBalance(BigDecimal balance) {
        CardBalanceResponseDto balanceDto = new CardBalanceResponseDto();
        balanceDto.setBalance(balance);
        return balanceDto;
    }
}

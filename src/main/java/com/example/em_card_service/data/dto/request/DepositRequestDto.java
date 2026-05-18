package com.example.em_card_service.data.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@EqualsAndHashCode
public class DepositRequestDto {

    @Schema(example = "10")
    @Positive
    @Digits(integer = Integer.MAX_VALUE, fraction = 2)
    private BigDecimal amount;

    public static DepositRequestDto forAmount(BigDecimal amount) {
        DepositRequestDto depositDto = new DepositRequestDto();
        depositDto.setAmount(amount);
        return depositDto;
    }
}

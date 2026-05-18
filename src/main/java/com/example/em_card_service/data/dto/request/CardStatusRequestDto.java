package com.example.em_card_service.data.dto.request;

import com.example.em_card_service.data.CardStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class CardStatusRequestDto {

    private CardStatus status;

    public static CardStatusRequestDto forStatus(CardStatus status) {
        CardStatusRequestDto statusDto = new CardStatusRequestDto();
        statusDto.setStatus(status);
        return statusDto;
    }
}

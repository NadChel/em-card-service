package com.example.em_card_service.mapper;

import com.example.em_card_service.data.dto.request.CardRequestDto;
import com.example.em_card_service.data.dto.response.CardBalanceResponseDto;
import com.example.em_card_service.data.dto.response.CardResponseDto;
import com.example.em_card_service.data.entity.Card;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @BeanMapping(builder = @Builder(disableBuilder = true))
    Card toCard(CardRequestDto cardRequestDto);

    @Mapping(source = "owner.id", target = "ownerId")
    CardResponseDto toDto(Card card);

    CardBalanceResponseDto toBalanceDto(Card card);
}

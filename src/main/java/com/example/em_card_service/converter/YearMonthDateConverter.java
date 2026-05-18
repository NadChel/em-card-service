package com.example.em_card_service.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;
import java.time.YearMonth;

@Converter(autoApply = true)
public class YearMonthDateConverter implements AttributeConverter<YearMonth, LocalDate> {

    @Override
    public LocalDate convertToDatabaseColumn(YearMonth yearMonth) {
        if (yearMonth == null) return null;
        return yearMonth.atDay(1);
    }

    @Override
    public YearMonth convertToEntityAttribute(LocalDate date) {
        if (date == null) return null;
        return YearMonth.from(date);
    }
}
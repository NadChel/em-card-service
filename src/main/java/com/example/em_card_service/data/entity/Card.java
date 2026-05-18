package com.example.em_card_service.data.entity;

import com.example.em_card_service.data.CardStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String number;
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private CardStatus status;
    private BigDecimal balance;
    private YearMonth expirationDate;
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    public Card() {
        this.status = CardStatus.ACTIVE;
        this.balance = BigDecimal.ZERO;
    }
}

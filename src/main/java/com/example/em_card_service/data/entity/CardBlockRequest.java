package com.example.em_card_service.data.entity;

import com.example.em_card_service.data.RequestStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class CardBlockRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private RequestStatus status;
    @OneToOne
    @JoinColumn(name = "card_id", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Card card;
    @CreatedDate
    private Instant createdAt;

    public static CardBlockRequest from(Card card) {
        CardBlockRequest request = new CardBlockRequest();
        request.setStatus(RequestStatus.PENDING);
        request.setCard(card);
        return request;
    }
}

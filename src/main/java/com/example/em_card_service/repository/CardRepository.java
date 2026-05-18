package com.example.em_card_service.repository;

import com.example.em_card_service.data.entity.Card;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.RepositoryDefinition;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RepositoryDefinition(idClass = UUID.class, domainClass = Card.class)
public interface CardRepository {

    List<Card> findAll(Pageable pageable);

    Optional<Card> findById(UUID id);

    List<Card> findByOwnerId(UUID userId, Pageable pageable);

    void deleteById(UUID id);

    Card save(Card card);
}

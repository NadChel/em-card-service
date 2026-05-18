package com.example.em_card_service.repository;

import com.example.em_card_service.data.entity.CardBlockRequest;
import org.springframework.data.repository.RepositoryDefinition;

import java.util.UUID;

@RepositoryDefinition(idClass = UUID.class, domainClass = CardBlockRequest.class)
public interface CardBlockRequestRepository {

    void save(CardBlockRequest request);
}

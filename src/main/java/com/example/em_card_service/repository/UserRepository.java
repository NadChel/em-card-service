package com.example.em_card_service.repository;

import com.example.em_card_service.data.entity.User;
import org.springframework.data.repository.RepositoryDefinition;

import java.util.Optional;
import java.util.UUID;

@RepositoryDefinition(idClass = UUID.class, domainClass = User.class)
public interface UserRepository {

    Optional<User> findById(UUID id);
}

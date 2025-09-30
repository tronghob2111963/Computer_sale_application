package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Integer> {

    // Xóa/tìm theo username
    Optional<Token> findByUsername(String username);

    // Xóa/tìm theo access_token
    Optional<Token> findByAccessToken(String accessToken);
}
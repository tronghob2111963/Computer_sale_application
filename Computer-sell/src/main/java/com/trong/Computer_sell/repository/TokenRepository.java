package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<TokenEntity, Integer> {

    // Xóa/tìm theo username
    Optional<TokenEntity> findByUsername(String username);

    // Xóa/tìm theo access_token
    Optional<TokenEntity> findByAccessToken(String accessToken);
}
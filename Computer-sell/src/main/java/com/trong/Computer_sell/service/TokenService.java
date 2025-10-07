package com.trong.Computer_sell.service;

import com.trong.Computer_sell.exception.ResourceNotFoundException;
import com.trong.Computer_sell.model.TokenEntity;
import com.trong.Computer_sell.repository.TokenRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;



@Service
public record TokenService(TokenRepository tokenRepository) {

    /**
     * Get token by username
     *
     * @param username
     * @return token
     */
    public TokenEntity getByUsername(String username) {
        return tokenRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Not found token"));
    }

    /**
     * Save token to DB
     *
     * @param token
     * @return
     */
    public int save(TokenEntity token) {
        Optional<TokenEntity> optional = tokenRepository.findByUsername(token.getUsername());
        if (optional.isEmpty()) {
            tokenRepository.save(token);
            return token.getId();
        } else {
            TokenEntity t = optional.get();
            t.setAccessToken(token.getAccessToken());
            t.setRefreshToken(token.getRefreshToken());
            tokenRepository.save(t);
            return t.getId();
        }
    }

    /**
     * Delete token by username
     *
     * @param username
     */
    public void delete(String username) {
        TokenEntity token = getByUsername(username);
        tokenRepository.delete(token);
    }
}

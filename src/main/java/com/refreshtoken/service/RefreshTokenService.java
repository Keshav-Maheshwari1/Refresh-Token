package com.refreshtoken.service;

import com.refreshtoken.entities.RefreshToken;
import org.springframework.http.ResponseEntity;

public interface RefreshTokenService {
    ResponseEntity<RefreshToken> createToken(String email);
    ResponseEntity<Boolean> tokenIsValid(String refreshToken);
    ResponseEntity<?> refreshJwtToken(String refreshToken);
}

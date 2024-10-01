package com.refreshtoken.service.impl;

import com.refreshtoken.entities.RefreshToken;
import com.refreshtoken.models.JwtResponse;
import com.refreshtoken.reposiotry.RefreshTokenRepository;
import com.refreshtoken.security.JwtHelper;
import com.refreshtoken.service.RefreshTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    Logger logger = LoggerFactory.getLogger(RefreshTokenServiceImpl.class);
    private final long refreshTokenValidity=5*60*60*1000;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtHelper jwtHelper;


    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public ResponseEntity<RefreshToken> createToken(String email) {
        try {
            RefreshToken refreshToken = RefreshToken.builder()
                    .userEmail(email)
                    .refreshToken(UUID.randomUUID().toString())
                    .expireMs(Instant.now().plusMillis(refreshTokenValidity))
                    .build();
            RefreshToken oldToken = refreshTokenRepository.findByUserEmail(email);
            if(oldToken != null){
                oldToken.setExpireMs(Instant.now().plusMillis(refreshTokenValidity));
                return Optional.of(refreshTokenRepository.save(oldToken))
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.badRequest().body(null));
            }
            return Optional.of(refreshTokenRepository.save(refreshToken))
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.badRequest().body(null));
        }catch (Exception e){
            throw new RuntimeException("Error creating refresh token: "+e);
        }
    }

    public ResponseEntity<Boolean> tokenIsValid(String refreshToken) {
        return Optional.ofNullable(refreshTokenRepository.findByRefreshToken(refreshToken))
                .map(token -> {
                    logger.info("{} token ms: {}", Instant.now(), token.getExpireMs());
                    boolean isAfter = token.getExpireMs().isAfter(Instant.now());
                    if (!isAfter) {
                        // Delete the token only if it is expired
                        refreshTokenRepository.delete(token);
                        return ResponseEntity.ok(Boolean.FALSE);
                    }
                    return ResponseEntity.ok(Boolean.TRUE);
                })
                .orElse(ResponseEntity.badRequest().body(Boolean.FALSE));
    }

    public ResponseEntity<?> refreshJwtToken(String refreshToken) {
        return Optional.ofNullable(this.tokenIsValid(refreshToken).getBody())
                .map(valid-> {
                    if(!valid){
                        return ResponseEntity.badRequest().body("Invalid refresh token");
                    }
                    JwtResponse response = Optional.of(refreshTokenRepository.findByRefreshToken(refreshToken))
                            .map(token -> {
                                return JwtResponse.builder()
                                        .refreshToken(token.getRefreshToken())
                                        .jwtToken(this.jwtHelper.generateTokenFromUsername(token.getUserEmail()))
                                        .username(token.getUserEmail())
                                        .build();
                            }).orElse(null);
                    return ResponseEntity.ok(response);

                })
                .orElse(ResponseEntity.badRequest().body("Invalid refresh token"));
    }

}

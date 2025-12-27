package ru.mtuci.rbpo2025.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mtuci.rbpo2025.model.AppUser;
import ru.mtuci.rbpo2025.model.SessionStatus;
import ru.mtuci.rbpo2025.model.UserSession;
import ru.mtuci.rbpo2025.repository.UserSessionRepository;
import ru.mtuci.rbpo2025.security.JwtTokenProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserSessionRepository sessionRepository;

    public TokenService(JwtTokenProvider jwtTokenProvider, UserSessionRepository sessionRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public Map<String, String> generateTokenPair(AppUser user) {
        String sessionId = UUID.randomUUID().toString();
        List<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .toList();

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getUsername(),
                roles,
                user.getId()
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getUsername(),
                user.getId(),
                sessionId
        );

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(30);

        UserSession session = new UserSession()
                .setUser(user)
                .setRefreshToken(refreshToken)
                .setStatus(SessionStatus.ACTIVE)
                .setCreatedAt(now)
                .setExpiresAt(expiresAt);

        sessionRepository.save(session);

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );
    }

    @Transactional
    public Map<String, String> refreshTokenPair(String oldRefreshToken) {
        if (!jwtTokenProvider.validateToken(oldRefreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String tokenType = jwtTokenProvider.getTokenType(oldRefreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new RuntimeException("Token is not a refresh token");
        }

        UserSession session = sessionRepository
                .findByRefreshTokenAndStatus(oldRefreshToken, SessionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Session not found or inactive"));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setStatus(SessionStatus.EXPIRED);
            sessionRepository.save(session);
            throw new RuntimeException("Refresh token expired");
        }

        session.setStatus(SessionStatus.REVOKED);
        sessionRepository.save(session);

        AppUser user = session.getUser();
        List<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .toList();

        String newSessionId = UUID.randomUUID().toString();
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getUsername(),
                roles,
                user.getId()
        );

        String newRefreshToken = jwtTokenProvider.generateRefreshToken(
                user.getUsername(),
                user.getId(),
                newSessionId
        );

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(30);

        UserSession newSession = new UserSession()
                .setUser(user)
                .setRefreshToken(newRefreshToken)
                .setStatus(SessionStatus.ACTIVE)
                .setCreatedAt(now)
                .setExpiresAt(expiresAt);

        sessionRepository.save(newSession);

        return Map.of(
                "accessToken", newAccessToken,
                "refreshToken", newRefreshToken
        );
    }
}


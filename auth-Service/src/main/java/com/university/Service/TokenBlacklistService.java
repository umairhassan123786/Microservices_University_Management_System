package com.university.Service;
import com.university.Entities.InvalidatedToken;
import com.university.Repository.InvalidatedTokenRepository;
import com.university.Util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

@Service
public class TokenBlacklistService {

    @Autowired
    private InvalidatedTokenRepository invalidatedTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public void invalidateToken(String token) {
        try {
            if (invalidatedTokenRepository.existsByToken(token)) {
                return;
            }
            Date expiryDate = jwtUtil.getExpirationDateFromToken(token);
            InvalidatedToken invalidatedToken = new InvalidatedToken(token, expiryDate);
            invalidatedTokenRepository.save(invalidatedToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isTokenBlacklisted(String token) {
        try {
            return invalidatedTokenRepository.existsByToken(token);
        } catch (Exception e) {
            return false;
        }
    }
    @Scheduled(fixedRate = 1800000)
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            Date now = new Date();
            List<InvalidatedToken> expiredTokens = invalidatedTokenRepository.findByExpiryDateBefore(now);
            int deletedCount = expiredTokens.size();
            if (!expiredTokens.isEmpty()) {
                invalidatedTokenRepository.deleteAll(expiredTokens);
            } else {
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public long getBlacklistedTokenCount() {
        return invalidatedTokenRepository.count();
    }
}
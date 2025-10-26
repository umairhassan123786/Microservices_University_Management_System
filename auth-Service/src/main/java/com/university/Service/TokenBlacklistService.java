package com.university.Service;
import com.university.Entities.InvalidatedToken;
import com.university.Repository.InvalidatedTokenRepository;
import com.university.Util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class TokenBlacklistService {

    @Autowired
    private InvalidatedTokenRepository invalidatedTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // ✅ Token ko blacklist mein add karein
    public void invalidateToken(String token) {
        try {
            Date expiryDate = jwtUtil.getExpirationDateFromToken(token);

            InvalidatedToken invalidatedToken = new InvalidatedToken();
            invalidatedToken.setToken(token);
            invalidatedToken.setExpiryDate(expiryDate);
            invalidatedToken.setInvalidatedAt(new Date());

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (Exception e) {
            // Token invalid hai ya parse nahi ho raha
            System.out.println("❌ Token invalidate karne mein error: " + e.getMessage());
        }
    }

    // ✅ Check karein token blacklisted hai ya nahi
    public boolean isTokenBlacklisted(String token) {
        return invalidatedTokenRepository.existsByToken(token);
    }

    // ✅ Expired tokens automatically delete karein (har 30 minute mein)
    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void cleanupExpiredTokens() {
        try {
            List<InvalidatedToken> expiredTokens = invalidatedTokenRepository.findAll();
            Date now = new Date();

            int deletedCount = 0;
            for (InvalidatedToken token : expiredTokens) {
                if (token.getExpiryDate().before(now)) {
                    invalidatedTokenRepository.delete(token);
                    deletedCount++;
                }
            }

            if (deletedCount > 0) {
                System.out.println("✅ " + deletedCount + " expired tokens deleted from blacklist");
            }
        } catch (Exception e) {
            System.out.println("❌ Token cleanup error: " + e.getMessage());
        }
    }
}
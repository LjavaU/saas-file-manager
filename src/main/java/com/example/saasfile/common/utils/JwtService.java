package com.example.saasfile.common.utils;

import io.jsonwebtoken.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Component
@Slf4j
public class JwtService {

    private static final String DOWNLOAD_SUBJECT = "file-download";
    private static final String UPLOAD_SESSION_SUBJECT = "file-upload-session";

    private static final String BUCKET_CLAIM = "b";
    private static final String OBJECT_CLAIM = "o";

    private static final String FILE_ID_CLAIM = "f";
    private static final String FILE_SIZE_CLAIM = "s";
    private static final String FILE_MD5_CLAIM = "d";
    private static final String TOTAL_PARTS_CLAIM = "p";
    private static final String UPLOAD_MODE_CLAIM = "m";

    private final byte[] jwtSecretBytes;

    public JwtService(@Value("${app.security.jwt-download-secret}") String secret) {
        this.jwtSecretBytes = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String generateDownloadToken(String bucketName, String objectName, long expirySeconds) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirySeconds * 1000);
        return Jwts.builder()
            .setSubject(DOWNLOAD_SUBJECT)
            .claim(BUCKET_CLAIM, bucketName)
            .claim(OBJECT_CLAIM, objectName)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, jwtSecretBytes)
            .compact();
    }

    public Optional<Claims> validateAndParseToken(String token) {
        return parseClaims(token, DOWNLOAD_SUBJECT);
    }

    public String generateUploadSessionToken(Long fileId,
                                             String bucketName,
                                             String objectName,
                                             long fileSize,
                                             String fileMd5,
                                             int totalParts,
                                             String mode,
                                             long expirySeconds) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirySeconds * 1000);
        return Jwts.builder()
            .setSubject(UPLOAD_SESSION_SUBJECT)
            .claim(FILE_ID_CLAIM, fileId)
            .claim(BUCKET_CLAIM, bucketName)
            .claim(OBJECT_CLAIM, objectName)
            .claim(FILE_SIZE_CLAIM, fileSize)
            .claim(FILE_MD5_CLAIM, fileMd5)
            .claim(TOTAL_PARTS_CLAIM, totalParts)
            .claim(UPLOAD_MODE_CLAIM, mode)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, jwtSecretBytes)
            .compact();
    }

    public Optional<UploadSessionClaims> validateAndParseUploadToken(String token) {
        Optional<Claims> claimsOptional = parseClaims(token, UPLOAD_SESSION_SUBJECT);
        if (!claimsOptional.isPresent()) {
            return Optional.empty();
        }
        Claims claims = claimsOptional.get();
        try {
            Number fileId = claims.get(FILE_ID_CLAIM, Number.class);
            Number fileSize = claims.get(FILE_SIZE_CLAIM, Number.class);
            Number totalParts = claims.get(TOTAL_PARTS_CLAIM, Number.class);
            if (fileId == null || fileSize == null || totalParts == null) {
                return Optional.empty();
            }
            return Optional.of(new UploadSessionClaims(
                fileId.longValue(),
                claims.get(BUCKET_CLAIM, String.class),
                claims.get(OBJECT_CLAIM, String.class),
                fileSize.longValue(),
                claims.get(FILE_MD5_CLAIM, String.class),
                totalParts.intValue(),
                claims.get(UPLOAD_MODE_CLAIM, String.class)
            ));
        } catch (RuntimeException ex) {
            log.warn("Invalid upload token claims: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public String getBucket(Claims claims) {
        return claims.get(BUCKET_CLAIM, String.class);
    }

    public String getObject(Claims claims) {
        return claims.get(OBJECT_CLAIM, String.class);
    }

    private Optional<Claims> parseClaims(String token, String expectedSubject) {
        try {
            Claims claims = Jwts.parser()
                .setSigningKey(jwtSecretBytes)
                .parseClaimsJws(token)
                .getBody();
            if (!expectedSubject.equals(claims.getSubject())) {
                log.warn("Invalid JWT subject: expected={}, actual={}", expectedSubject, claims.getSubject());
                return Optional.empty();
            }
            return Optional.of(claims);
        } catch (ExpiredJwtException e) {
            log.info("Token expired");
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid token received: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Getter
    @AllArgsConstructor
    public static class UploadSessionClaims {
        private final long fileId;
        private final String bucketName;
        private final String objectName;
        private final long fileSize;
        private final String fileMd5;
        private final int totalParts;
        private final String mode;
    }
}

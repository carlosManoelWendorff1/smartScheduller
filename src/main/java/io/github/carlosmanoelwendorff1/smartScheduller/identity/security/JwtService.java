// identity/security/JwtService.java
package io.github.carlosmanoelwendorff1.smartScheduller.identity.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

@Component
public class JwtService {

    private static final String ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final SecretKeySpec signingKey;
    private final Duration tokenTtl;

    public JwtService(ObjectMapper objectMapper,
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.ttl-minutes:480}") long ttlMinutes) {
        this.objectMapper = objectMapper;
        this.signingKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        this.tokenTtl = Duration.ofMinutes(ttlMinutes);
    }

    public String generateToken(UUID userId, UUID tenantId, String role) {
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Instant now = Instant.now();
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", userId.toString());
        payload.put("tenant", tenantId.toString());
        payload.put("role", role);
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", now.plus(tokenTtl).getEpochSecond());

        String headerPart = encode(header);
        String payloadPart = encode(payload);
        String signature = sign(headerPart + "." + payloadPart);
        return headerPart + "." + payloadPart + "." + signature;
    }

    public JwtClaims parseAndValidate(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new InvalidJwtException("Malformed token.");
        }
        if (!sign(parts[0] + "." + parts[1]).equals(parts[2])) {
            throw new InvalidJwtException("Invalid token signature.");
        }

        Map<?, ?> payload = decode(parts[1]);
        long exp = ((Number) payload.get("exp")).longValue();
        if (Instant.now().getEpochSecond() > exp) {
            throw new InvalidJwtException("Token expired.");
        }

        return new JwtClaims(
                UUID.fromString((String) payload.get("sub")),
                UUID.fromString((String) payload.get("tenant")),
                (String) payload.get("role"));
    }

    private String encode(Map<String, Object> value) {
        return ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private Map<?, ?> decode(String segment) {
        try {
            return objectMapper.readValue(DECODER.decode(segment), Map.class);
        } catch (Exception ex) {
            throw new InvalidJwtException("Malformed token payload.");
        }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(signingKey);
            return ENCODER.encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Could not sign JWT.", ex);
        }
    }
}
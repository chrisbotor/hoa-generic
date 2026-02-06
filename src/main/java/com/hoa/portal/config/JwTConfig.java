package com.hoa.portal.config;

import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Dependent
public class JwtConfig { 

    @Produces
    public JWTAuthContextInfo contextInfo() {
        // This MUST match the SECRET in your AuthResource.java
        String secret = "my-super-secret-hoa-key-at-least-32-chars";
        
        JWTAuthContextInfo contextInfo = new JWTAuthContextInfo();
        contextInfo.setIssuedBy("https://hoa-portal.com");
        
        // Explicitly set the HS256 Secret Key
        SecretKeySpec key = new SecretKeySpec(
            secret.getBytes(StandardCharsets.UTF_8), 
            "HmacSHA256"
        );
        contextInfo.setSecretVerificationKey(key);
        contextInfo.setSignatureAlgorithm(Collections.singleton("HS256"));
        
        return contextInfo;
    }
}
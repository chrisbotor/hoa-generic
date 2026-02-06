package com.hoa.portal.config;

import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

        // Being extremely explicit with the type to avoid "incompatible bounds"
        Set<SignatureAlgorithm> algs = new HashSet<SignatureAlgorithm>();
        algs.add(SignatureAlgorithm.HS256);
        contextInfo.setSignatureAlgorithm(algs);
        
        return contextInfo;
    }
}
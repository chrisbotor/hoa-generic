package com.hoa.portal.config;

import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import jakarta.annotation.Priority;
import jakarta.interceptor.Interceptor;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Dependent
@Alternative // Mark this as an alternative to the default Quarkus bean
@Priority(Interceptor.Priority.APPLICATION + 10) // Give it higher priority
public class JwtConfig {

    @Produces
    @Alternative
    public JWTAuthContextInfo contextInfo() {
        String secret = "my-super-secret-hoa-key-at-least-32-chars";
        
        JWTAuthContextInfo contextInfo = new JWTAuthContextInfo();
        contextInfo.setIssuedBy("https://hoa-portal.com");
        
        SecretKeySpec key = new SecretKeySpec(
            secret.getBytes(StandardCharsets.UTF_8), 
            "HmacSHA256"
        );
        contextInfo.setSecretVerificationKey(key);

        Set<SignatureAlgorithm> algs = new HashSet<>();
        algs.add(SignatureAlgorithm.HS256);
        contextInfo.setSignatureAlgorithm(algs);
        
        return contextInfo;
    }
}
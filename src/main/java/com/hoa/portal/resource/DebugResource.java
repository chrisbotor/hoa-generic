package com.hoa.portal.resource;

import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@Path("/portal/debug")
public class DebugResource {

    @Inject
    JWTAuthContextInfo authContextInfo;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> debugSecurity() {
        Map<String, Object> debugInfo = new HashMap<>();
        
        try {
            debugInfo.put("issuer", authContextInfo.getIssuedBy());
            debugInfo.put("signatureAlgorithm", authContextInfo.getSignatureAlgorithm());
            debugInfo.put("isRelaxed", authContextInfo.isRelaxVerificationKeyValidation());
            
            // Check if key is actually loaded (will show true/false without revealing the key)
            debugInfo.put("hasSecretKey", authContextInfo.getSecretVerificationKey() != null);
            
            // Helpful for Uber-Jar debugging
            debugInfo.put("javaVersion", System.getProperty("java.version"));
        } catch (Exception e) {
            debugInfo.put("error", e.getMessage());
        }
        
        return debugInfo;
    }
}
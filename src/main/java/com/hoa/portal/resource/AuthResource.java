package com.hoa.portal.resource;

import com.hoa.portal.entity.User;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// IMPORTANT: Use SecretKey for symmetric signing (HS256)
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.HashSet;
import java.util.Map;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @ConfigProperty(name = "JWT_SECRET")
    String jwtSecret;

    @POST
    @Path("/login")
    public Response login(LoginRequest loginRequest) {
        User user = User.find("email", loginRequest.email).firstResult();

        if (user != null && BcryptUtil.matches(loginRequest.passwordHash, user.passwordHash)) {
            
            // 1. Convert the secret string to raw bytes
            byte[] keyBytes = jwtSecret.trim().getBytes(StandardCharsets.UTF_8);
            
            // 2. Explicitly define as SecretKey to satisfy the .sign() method
            SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");

            String token = Jwt.issuer("https://hoa-portal.com")
                .upn(user.email)
                .groups(new HashSet<>(Arrays.asList(user.role)))
                .claim("https://hasura.io/jwt/claims", Map.of(
                    "x-hasura-allowed-roles", Arrays.asList(user.role),
                    "x-hasura-default-role", user.role,
                    "x-hasura-user-id", user.id.toString()
                ))
                .expiresIn(28800)
                .sign(key); // This will now compile correctly

            return Response.ok(Map.of("token", token)).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    public static class LoginRequest {
        public String email;
        public String passwordHash;
    }
}
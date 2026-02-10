package com.hoa.portal.resource;

import com.hoa.portal.entity.User;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
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
            
            // 1. Get raw bytes from the secret (we'll treat it as a raw string for simplicity)
            byte[] keyBytes = jwtSecret.trim().getBytes(StandardCharsets.UTF_8);
            
            // 2. Create a proper HMAC SHA-256 Key object
            // This ensures the signer uses the exact bits required for HS256
            Key key = new SecretKeySpec(keyBytes, "HmacSHA256");

            String token = Jwt.issuer("https://hoa-portal.com")
                .upn(user.email)
                .groups(new HashSet<>(Arrays.asList(user.role)))
                .claim("https://hasura.io/jwt/claims", Map.of(
                    "x-hasura-allowed-roles", Arrays.asList(user.role),
                    "x-hasura-default-role", user.role,
                    "x-hasura-user-id", user.id.toString()
                ))
                .expiresIn(28800)
                .sign(key); // Sign using the Key object instead of a string

            return Response.ok(Map.of("token", token)).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    public static class LoginRequest {
        public String email;
        public String passwordHash;
    }
}
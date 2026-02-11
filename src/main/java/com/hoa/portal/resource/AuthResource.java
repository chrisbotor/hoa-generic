package com.hoa.portal.resource;

import com.hoa.portal.entity.User;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    // Base64 for: "this-is-a-32-char-secret-key-123"
    private static final String BASE64_SECRET = "dGhpcy1pcy1hLTMyLWNoYXItc2VjcmV0LWtleS0xMjM=";

    @POST
    @Path("/login")
    public Response login(LoginRequest loginRequest) {
        User user = User.find("email", loginRequest.email).firstResult();

        if (user != null && BcryptUtil.matches(loginRequest.passwordHash, user.passwordHash)) {
            
            // Decode Base64 to get the actual bytes for the HMAC-SHA256 signature
            byte[] keyBytes = Base64.getDecoder().decode(BASE64_SECRET);

            String token = Jwt.issuer("https://hoa-portal.com")
                .upn(user.email)
                .groups(new HashSet<>(Arrays.asList(user.role)))
                .claim("https://hasura.io/jwt/claims", Map.of(
                    "x-hasura-allowed-roles", Arrays.asList(user.role),
                    "x-hasura-default-role", user.role,
                    "x-hasura-user-id", user.id.toString()
                ))
                .expiresIn(28800)
                // Passing the decoded bytes directly ensures a match with Quarkus properties
                .signWithSecret(new String(keyBytes, StandardCharsets.UTF_8));

            return Response.ok(Map.of("token", token)).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    public static class LoginRequest {
        public String email;
        public String passwordHash;
    }
}
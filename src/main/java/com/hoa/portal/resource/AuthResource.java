package com.hoa.portal.resource;

import com.hoa.portal.entity.User;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
            
            // Convert Hex String to Byte Array to ensure 100% signature match
            byte[] secretBytes = decodeHex(jwtSecret.trim());

            String token = Jwt.issuer("https://hoa-portal.com")
                .upn(user.email)
                .groups(new HashSet<>(Arrays.asList(user.role)))
                .claim("https://hasura.io/jwt/claims", Map.of(
                    "x-hasura-allowed-roles", Arrays.asList(user.role),
                    "x-hasura-default-role", user.role,
                    "x-hasura-user-id", user.id.toString()
                ))
                .expiresIn(28800)
                // Use the raw bytes for signing
                .signWithSecret(new String(secretBytes, StandardCharsets.UTF_8));

            return Response.ok(Map.of("token", token)).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    /**
     * Helper to convert Hex string from Environment Variable to byte array
     */
    private byte[] decodeHex(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }

    public static class LoginRequest {
        public String email;
        public String passwordHash;
    }
}
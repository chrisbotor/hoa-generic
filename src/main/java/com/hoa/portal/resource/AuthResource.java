package com.hoa.portal.resource;

import com.hoa.portal.entity.User;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Map;
import java.util.Arrays;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @POST
    @Path("/login")
    public Response login(LoginRequest loginRequest) {
        System.out.println("DEBUG: Login attempt for email: " + loginRequest.email);
        
        if (loginRequest == null || loginRequest.email == null || loginRequest.passwordHash == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(Map.of("error", "Email and passwordHash are required")).build();
        }

        // 1. Find user in the 'hoa' schema
        User user = User.find("email", loginRequest.email).firstResult();

        if (user == null) {
            System.out.println("DEBUG: User NOT found for: " + loginRequest.email);
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // 2. Deep Debugging Lengths and Whitespace
        String rawInput = loginRequest.passwordHash.trim();
        String dbHash = user.passwordHash.trim();
        
        System.out.println("DEBUG: Raw Input Length (trimmed): " + rawInput.length());
        System.out.println("DEBUG: DB Hash Length (trimmed): " + dbHash.length());
        
        // TEMPORARY GENERATOR: Use this to see what the app wants the hash to be
        String expectedHash = BcryptUtil.bcryptHash(rawInput);
        System.out.println("DEBUG: GENERATED HASH for '" + rawInput + "' is: " + expectedHash);

        // 3. Perform the match
        boolean matches = BcryptUtil.matches(rawInput, dbHash);
        System.out.println("DEBUG: Bcrypt matching result: " + matches);

        if (matches) {
            String token = Jwt.issuer("https://hoa-portal.com")
                .upn(user.email)
                .groups(new HashSet<>(Arrays.asList(user.role)))
                .claim("https://hasura.io/jwt/claims", Map.of(
                    "x-hasura-allowed-roles", Arrays.asList(user.role),
                    "x-hasura-default-role", user.role,
                    "x-hasura-user-id", user.id.toString(),
                    "x-hasura-house-id", user.houseId != null ? user.houseId.toString() : "0"
                ))
                .expiresIn(28800) 
                .signWithSecret("my-super-secret-hoa-key-at-least-32-chars");

            return Response.ok(Map.of("token", token)).build();
        }

        return Response.status(Response.Status.UNAUTHORIZED)
                       .entity(Map.of("error", "Invalid credentials")).build();
    }

    public static class LoginRequest {
        public String email;
        public String passwordHash; 
    }
}
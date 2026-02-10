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
        // Log the incoming request details
        System.out.println("DEBUG: Login attempt for email: " + loginRequest.email);
        
        if (loginRequest == null || loginRequest.email == null || loginRequest.passwordHash == null) {
            System.out.println("DEBUG: Failed - Missing email or passwordHash in payload.");
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(Map.of("error", "Email and passwordHash are required")).build();
        }

        // 1. Search for user in the 'hoa' schema
        User user = User.find("email", loginRequest.email).firstResult();

        if (user == null) {
            System.out.println("DEBUG: Failed - User not found in database for email: " + loginRequest.email);
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity(Map.of("error", "Invalid credentials")).build();
        }

        // 2. Log found user and hash for verification
        System.out.println("DEBUG: User found: " + user.fullName + " (ID: " + user.id + ")");
        System.out.println("DEBUG: Stored Hash in DB: " + user.passwordHash);

        // 3. Verify Bcrypt match
        boolean matches = BcryptUtil.matches(loginRequest.passwordHash, user.passwordHash);
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

            System.out.println("DEBUG: Login Success - JWT Generated.");
            return Response.ok(Map.of("token", token)).build();
        }

        System.out.println("DEBUG: Failed - Password mismatch for " + loginRequest.email);
        return Response.status(Response.Status.UNAUTHORIZED)
                       .entity(Map.of("error", "Invalid credentials")).build();
    }

    public static class LoginRequest {
        public String email;
        public String passwordHash; 
    }
}
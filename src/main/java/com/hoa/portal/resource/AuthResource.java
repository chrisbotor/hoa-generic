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

    // Simple, alpha-numeric string to avoid encoding issues.
    // Ensure this matches application.properties exactly.
    private static final String JWT_SECRET = "StationBeelinkSer5ProHOAKey2026!";

    @POST
    @Path("/login")
    public Response login(LoginRequest loginRequest) {
        // Find user by email in the database
        User user = User.find("email", loginRequest.email).firstResult();

        // Verify user exists and Bcrypt password matches
        if (user != null && BcryptUtil.matches(loginRequest.passwordHash, user.passwordHash)) {
            
            // Generate the JWT with Hasura-compatible claims
            String token = Jwt.issuer("https://hoa-portal.com")
                .upn(user.email)
                .groups(new HashSet<>(Arrays.asList(user.role)))
                .claim("https://hasura.io/jwt/claims", Map.of(
                    "x-hasura-allowed-roles", Arrays.asList(user.role),
                    "x-hasura-default-role", user.role,
                    "x-hasura-user-id", user.id.toString()
                ))
                .expiresIn(28800) // 8 hours
                .signWithSecret(JWT_SECRET);

            return Response.ok(Map.of("token", token)).build();
        }
        
        // Return 401 if login fails
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    public static class LoginRequest {
        public String email;
        public String passwordHash;
    }
}
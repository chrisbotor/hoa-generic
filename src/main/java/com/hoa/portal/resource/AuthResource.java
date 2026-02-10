package com.hoa.portal.resource;

import com.hoa.portal.entity.User;
import io.quarkus.elytron.security.common.BcryptUtil; // For password matching
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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
        // 1. Find the user by email
        User user = User.find("email", loginRequest.email).firstResult();

        // 2. Verify user exists and Bcrypt password matches
        if (user != null && BcryptUtil.matches(loginRequest.password, user.passwordHash)) {
            
            // 3. Generate the token with Hasura claims
            String token = Jwt.issuer("https://hoa-portal.com")
                .upn(user.email)
                .groups(new HashSet<>(Arrays.asList(user.role)))
                .claim("https://hasura.io/jwt/claims", Map.of(
                    "x-hasura-allowed-roles", Arrays.asList(user.role),
                    "x-hasura-default-role", user.role,
                    "x-hasura-user-id", user.id.toString(),
                    "x-hasura-house-id", user.houseId != null ? user.houseId.toString() : "0"
                ))
                .expiresIn(28800) // 8 hours
                .signWithSecret("my-super-secret-hoa-key-at-least-32-chars");

            return Response.ok(Map.of("token", token)).build();
        }

        return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }
}
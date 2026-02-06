package com.hoa.portal.resource;

import com.hoa.portal.entity.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Arrays;
import java.util.HashMap;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    // Must be at least 32 characters for HS256
    private static final String SECRET = "my-super-secret-hoa-key-at-least-32-chars";

    @POST
    @Path("/login")
    public Response login(User credentials) {
        // 1. Fetch user from the 'hoa.users' table
        User user = User.findByEmail(credentials.email);

        System.out.println("DEBUG:  UUID is: " + user.id.toString());

        // 2. Simple password check (For MVP)
        if (user != null && user.passwordHash.equals(credentials.passwordHash)) {
            
            // 3. Create the SecretKey explicitly to satisfy the compiler
            SecretKey key = new SecretKeySpec(
                SECRET.getBytes(StandardCharsets.UTF_8), 
                "HmacSHA256"
            );

            // 4. Build the JWT with Hasura-specific claims
            String token = Jwt.issuer("hoa-auth")
                .upn(user.email)
                .groups(new HashSet<>(Arrays.asList(user.role)))
                .claim("https://hasura.io/jwt/claims", new HashMap<String, Object>() {{
                    put("x-hasura-default-role", user.role);
                    put("x-hasura-allowed-roles", Arrays.asList("resident", "admin"));
                    put("x-hasura-user-id", user.id.toString());
                }})
                .sign(key); // This now matches the sign(SecretKey) method

            return Response.ok(new AuthResponse(token, user.role)).build();
        }

        // Return 401 if login fails
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    public static class AuthResponse {
        public String token;
        public String role;
        public AuthResponse(String token, String role) {
            this.token = token;
            this.role = role;
        }
    }
}
package com.hoa.portal.resource;

import com.hoa.portal.entity.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.HashSet;
import java.util.Arrays;
import java.util.HashMap;

@Path("/portal/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    // Your secret key must be at least 32 characters for HS256
    private static final String SECRET = "my-super-secret-hoa-key-at-least-32-chars";

    @POST
    @Path("/login")
    public Response login(User credentials) {
        User user = User.findByEmail(credentials.email);

        if (user != null && user.passwordHash.equals(credentials.passwordHash)) {
            
            // Create the key manually to avoid SRJWT05028
            Key key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

            String token = Jwt.issuer("https://hoa-portal.com")
                .upn(user.email)
                .groups(new HashSet<>(Arrays.asList(user.role)))
                .claim("https://hasura.io/jwt/claims", new HashMap<String, Object>() {{
                    put("x-hasura-default-role", user.role);
                    put("x-hasura-allowed-roles", Arrays.asList("resident", "admin"));
                    put("x-hasura-user-id", user.id.toString());
                }})
                .sign(key); // Sign using the explicit key object

            return Response.ok(new AuthResponse(token, user.role)).build();
        }

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
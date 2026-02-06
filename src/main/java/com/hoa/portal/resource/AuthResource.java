package com.hoa.portal.resource;

import com.hoa.portal.entity.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Arrays;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @POST
    @Path("/login")
    public Response login(User credentials) {
        // 1. Find user in Postgres
        User user = User.findByEmail(credentials.email);

        // 2. Validate Password (In MVP, we use plain text comparison for now)
        if (user != null && user.passwordHash.equals(credentials.passwordHash)) {
            
            // 3. Build the JWT with Hasura Claims
            String token = Jwt.issuer("https://hoa-portal.com")
                .upn(user.email)
                .groups(new HashSet<>(Arrays.asList(user.role)))
                .claim("https://hasura.io/jwt/claims", 
                    new java.util.HashMap<String, Object>() {{
                        put("x-hasura-default-role", user.role);
                        put("x-hasura-allowed-roles", Arrays.asList("resident", "admin"));
                        put("x-hasura-user-id", user.id.toString());
                    }})
                .sign();

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
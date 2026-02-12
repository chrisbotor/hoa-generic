package com.hoa.portal.resource;

import com.hoa.portal.entity.User;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Map;
import java.util.Arrays;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    // Hardcoded for absolute synchronization with application.properties
    private static final String JWT_SECRET = "BeelinkSer5ProHOAKey_Static2026!";

    @PostConstruct
    public void debugSecret() {
        System.out.println("========================================");
        System.out.println("DEBUG: JWT_SECRET length: " + JWT_SECRET.length()); 
        System.out.println("========================================");
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest loginRequest) {
        // 1. Find the user
        User user = User.find("email", loginRequest.email).firstResult();

        // 2. Bcrypt Check: matches(PlainTextFromUser, HashFromDatabase)
        if (user != null && BcryptUtil.matches(loginRequest.passwordHash, user.passwordHash)) {
            
            // 3. Generate Token
            String token = Jwt.issuer("https://hoa-portal.com")
                .upn(user.email)    
                .subject(user.email)
                .groups(new HashSet<>(Arrays.asList(user.role)))
                .claim("https://hasura.io/jwt/claims", Map.of(
                    "x-hasura-allowed-roles", Arrays.asList(user.role),
                    "x-hasura-default-role", user.role,
                    "x-hasura-user-id", user.id.toString()
                ))
                .expiresIn(28800) 
                .signWithSecret(JWT_SECRET); 

            return Response.ok(Map.of("token", token)).build();
        }
        
        // Fail if no match
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    public static class LoginRequest {
        public String email;
        public String passwordHash; // This will receive the plain text "password123"
    }
}
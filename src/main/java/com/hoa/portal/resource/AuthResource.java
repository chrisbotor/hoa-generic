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

    // Exactly 32 characters to satisfy the 256-bit requirement for HS256
    private static final String JWT_SECRET = "StationBeelinkSer5ProHOAKey2026!";

    /**
     * This method runs as soon as the application starts.
     * Use this to check the logs and confirm if the 32nd character is being read.
     */
    @PostConstruct
    public void debugSecretOnStartup() {
        int length = JWT_SECRET.length();
        System.out.println("========================================");
        System.out.println("DEBUG: JWT_SECRET length: " + length);
        if (length < 32) {
            System.err.println("ERROR: Key is ONLY " + (length * 8) + " bits. Needs 256!");
        } else {
            System.out.println("SUCCESS: Key is " + (length * 8) + " bits.");
        }
        System.out.println("========================================");
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest loginRequest) {
        User user = User.find("email", loginRequest.email).firstResult();

        if (user != null && BcryptUtil.matches(loginRequest.passwordHash, user.passwordHash)) {
            
            // Generate the token using the verified secret
            String token = Jwt.issuer("https://hoa-portal.com")
                .upn(user.email)
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
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    public static class LoginRequest {
        public String email;
        public String passwordHash;
    }
}
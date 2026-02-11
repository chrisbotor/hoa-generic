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

    // Hardcoded for synchronization test
    private static final String JWT_SECRET = "StationBeelinkSer5ProHOAKey2026!";

    @PostConstruct
    public void debugSecret() {
        System.out.println("========================================");
        System.out.println("DEBUG: JWT_SECRET length: " + JWT_SECRET.length());
        System.out.println("========================================");
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest loginRequest) {
        User user = User.find("email", loginRequest.email).firstResult();

        if (user != null && BcryptUtil.matches(loginRequest.passwordHash, user.passwordHash)) {
            
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
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    public static class LoginRequest {
        public String email;
        public String passwordHash;
    }
}
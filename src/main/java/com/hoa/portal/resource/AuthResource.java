package com.hoa.portal.resource;

import com.hoa.portal.entity.User;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.annotation.security.PermitAll;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    // Must be exactly 32 characters to match your deployment environment
    private static final String JWT_SECRET = "BeelinkSer5ProHOAKeyStatic202626";

    @POST
    @Path("/login")
    @PermitAll
    public Response login(LoginRequest request) {
        // 1. Find the user
        User user = User.find("email", request.email).firstResult();

        // 2. Validate password and existence
        if (user == null || !BcryptUtil.matches(request.passwordHash, user.passwordHash)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // 3. Prepare roles
        Set<String> roles = new HashSet<>();
        roles.add(user.role); // e.g., "resident" or "admin"

        try {
            // 4. Create the SecretKeySpec (The "Key Maker")
            // This ensures we use UTF-8 bytes specifically
            byte[] keyBytes = JWT_SECRET.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");

            // 5. Build and Sign the Token
            String token = Jwt.issuer("https://hoa-portal.com")
                    .upn(user.email)
                    .subject(user.email)
                    .groups(roles)
                    .expiresIn(28800)
                    .claim("https://hasura.io/jwt/claims", Map.of(
                            "x-hasura-allowed-roles", roles,
                            "x-hasura-default-role", user.role,
                            "x-hasura-user-id", user.id.toString()
                    ))
                    .sign(secretKey); // Uses the explicit Key object

            return Response.ok(Map.of("token", token)).build();

        } catch (Exception e) {
            return Response.serverError().entity("Error generating token: " + e.getMessage()).build();
        }
    }

    public static class LoginRequest {
        public String email;
        public String passwordHash; 
    }
}
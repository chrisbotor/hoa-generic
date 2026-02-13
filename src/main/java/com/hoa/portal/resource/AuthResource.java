package com.hoa.portal.resource;

import com.hoa.portal.entity.User;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.algorithm.SignatureAlgorithm;
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

    // Exactly 32 characters
    private static final String JWT_SECRET = "BeelinkSer5ProHOAKeyStatic202626";

    @POST
    @Path("/login")
    @PermitAll
    public Response login(LoginRequest request) {
        User user = User.find("email", request.email).firstResult();

        // Note: verify if your User entity uses .password or .passwordHash
        if (user == null || !BcryptUtil.matches(request.passwordHash, user.passwordHash)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Set<String> roles = new HashSet<>();
        roles.add(user.role);

        try {
            // Convert secret string to byte array
            byte[] keyBytes = JWT_SECRET.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");

            String token = Jwt.issuer("https://hoa-portal.com")
                    .upn(user.email)
                    .subject(user.email)
                    .groups(roles)
                    .expiresIn(28800) // 8 hours
                    .claim("https://hasura.io/jwt/claims", Map.of(
                            "x-hasura-allowed-roles", roles,
                            "x-hasura-default-role", user.role,
                            "x-hasura-user-id", user.id.toString()
                    ))
                    .jws()
                    .algorithm(SignatureAlgorithm.HS256) // Forces HS256 in the Header
                    .sign(secretKey); // Signs with the specific Key object

            return Response.ok(Map.of("token", token)).build();

        } catch (Exception e) {
            return Response.serverError().entity("Token Error: " + e.getMessage()).build();
        }
    }

    public static class LoginRequest {
        public String email;
        public String passwordHash; 
    }
}
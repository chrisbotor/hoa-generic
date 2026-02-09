package com.hoa.portal.resource;

import com.hoa.portal.entity.User;
import com.hoa.portal.model.AuthRequest;
import com.hoa.portal.model.AuthResponse;
import io.smallrye.jwt.build.Jwt;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final String key = "my-super-secret-hoa-key-at-least-32-chars";

    @POST
    @Path("/login")
    public Response login(AuthRequest credentials) {
        // Look up the user in the .80 database
        User user = User.find("email", credentials.getEmail()).firstResult();

        if (user == null || !user.passwordHash.equals(credentials.getPasswordHash())) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Build Hasura-compatible claims dynamically
        Map<String, Object> hasuraClaims = new HashMap<>();
        hasuraClaims.put("x-hasura-default-role", user.role);
        hasuraClaims.put("x-hasura-allowed-roles", List.of(user.role));
        hasuraClaims.put("x-hasura-user-id", user.id.toString());
        hasuraClaims.put("x-hasura-house-id", user.houseId);

        String token = Jwt.issuer("hoa-auth")
                .upn(user.email)
                .groups(user.role)
                .claim("https://hasura.io/jwt/claims", hasuraClaims)
                .sign(key);

        return Response.ok(new AuthResponse(token, user.role)).build();
    }
}
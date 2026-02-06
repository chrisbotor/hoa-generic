package com.hoa.portal.resource;

import com.hoa.portal.entity.MaintenanceRequest;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;
import io.vertx.core.json.JsonObject;

@Path("/requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MaintenanceRequestResource {

    @Inject
    SecurityIdentity identity;

    @Inject
    JsonWebToken jwt; // Use the raw JWT for nested claims

    @GET
    @RolesAllowed({"admin", "resident"})
    public List<MaintenanceRequest> getRequests() {
        if (identity.getRoles().contains("admin")) {
            return MaintenanceRequest.listAll();
        }

        try {
            // 1. Get the nested Hasura claims object
            JsonObject hasuraClaims = jwt.getClaim("https://hasura.io/jwt/claims");
            
            if (hasuraClaims != null) {
                // 2. Extract the UUID string specifically
                String userIdStr = hasuraClaims.getString("x-hasura-user-id");
                
                if (userIdStr != null) {
                    System.out.println("Success! Filtering for UUID: " + userIdStr);
                    return MaintenanceRequest.find("requesterId", UUID.fromString(userIdStr)).list();
                }
            }
            
            System.err.println("Claim x-hasura-user-id not found in token.");
            
        } catch (Exception e) {
            System.err.println("Security Error: " + e.getMessage());
        }

        return List.of();
    }
}
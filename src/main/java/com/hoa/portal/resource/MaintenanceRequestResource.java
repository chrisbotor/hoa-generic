package com.hoa.portal.resource;

import com.hoa.portal.entity.MaintenanceRequest;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.json.JsonObject; // Use Jakarta instead of Vert.x
import java.util.List;
import java.util.UUID;

@Path("/requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MaintenanceRequestResource {

    @Inject
    SecurityIdentity identity;

    @Inject
    JsonWebToken jwt;

    @GET
    @RolesAllowed({"admin", "resident"})
    public List<MaintenanceRequest> getRequests() {
        if (identity.getRoles().contains("admin")) {
            return MaintenanceRequest.listAll();
        }

        try {
            // SmallRye JWT returns Jakarta JsonObject for nested claims
            JsonObject hasuraClaims = jwt.getClaim("https://hasura.io/jwt/claims");
            
            if (hasuraClaims != null) {
                // Jakarta JsonObject uses getString()
                String userIdStr = hasuraClaims.getString("x-hasura-user-id");
                
                if (userIdStr != null && !userIdStr.isEmpty()) {
                    System.out.println("Success! Validated UUID from Claim: " + userIdStr);
                    return MaintenanceRequest.find("requesterId", UUID.fromString(userIdStr)).list();
                }
            }
            
            System.err.println("Claim x-hasura-user-id was not found in the token.");
            
        } catch (Exception e) {
            // This will now catch and print the specific reason if it fails
            System.err.println("Maintenance Resource Error: " + e.getMessage());
            e.printStackTrace();
        }

        return List.of();
    }
}
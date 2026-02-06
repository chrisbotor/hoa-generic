package com.hoa.portal.resource;

import com.hoa.portal.entity.MaintenanceRequest;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MaintenanceRequestResource {

    @Inject
    SecurityIdentity identity;

    @GET
    @RolesAllowed({"admin", "resident"})
    public List<MaintenanceRequest> getRequests() {
        // 1. Log the principal for Beelink terminal debugging
        String principalName = identity.getPrincipal().getName();
        System.out.println("Processing GET /requests for: " + principalName);

        // 2. Admin Role: Fetch all records without filtering
        if (identity.getRoles().contains("admin")) {
            System.out.println("Admin access: returning all community requests.");
            return MaintenanceRequest.listAll();
        }

        // 3. Resident Role: Filter by the UUID inside the nested Hasura claims
        try {
            // Extract the map we saw in your JWT.io debug
            Object claimsObj = identity.getAttribute("https://hasura.io/jwt/claims");
            
            String userIdStr = null;

            if (claimsObj instanceof Map) {
                Map<String, Object> claims = (Map<String, Object>) claimsObj;
                userIdStr = (String) claims.get("x-hasura-user-id");
            } 
            
            // Fallback: If map extraction fails, try using the principal name (often the 'sub')
            if (userIdStr == null) {
                userIdStr = principalName;
            }

            if (userIdStr != null && !userIdStr.isEmpty()) {
                System.out.println("Resident access: filtering for UUID " + userIdStr);
                
                // Ensure your Entity uses "requesterId" as the field name
                return MaintenanceRequest.find("requesterId", UUID.fromString(userIdStr)).list();
            } else {
                System.err.println("Failed to identify User ID for resident: " + principalName);
            }

        } catch (IllegalArgumentException e) {
            System.err.println("Invalid UUID format received: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Database query error: " + e.getMessage());
            e.printStackTrace();
        }

        return List.of();
    }
}
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

@Path("/requests") // Root path /portal is handled by application.properties
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MaintenanceRequestResource {

    @Inject
    SecurityIdentity identity;

    @GET
    @RolesAllowed({"admin", "resident"})
    public List<MaintenanceRequest> getRequests() {
        // Log the user for debugging in your Beelink terminal
        System.out.println("Processing request for: " + identity.getPrincipal().getName());
        System.out.println("User Roles: " + identity.getRoles());

        // 1. If the user is an Admin, show all community requests
        if (identity.getRoles().contains("admin")) {
            System.out.println("Role: Admin - Fetching all records");
            return MaintenanceRequest.listAll();
        }
        
        // 2. If a Resident, filter by their specific UUID stored in the JWT
        try {
            // We pull the Hasura-style claims map we created in AuthResource
            Map<String, Object> claims = identity.getAttribute("https://hasura.io/jwt/claims");
            
            if (claims != null && claims.containsKey("x-hasura-user-id")) {
                String userIdStr = (String) claims.get("x-hasura-user-id");
                System.out.println("Role: Resident - Filtering for ID: " + userIdStr);
                
                return MaintenanceRequest.find("requesterId", UUID.fromString(userIdStr)).list();
            }
        } catch (Exception e) {
            System.err.println("Error extracting claims: " + e.getMessage());
        }

        // Return empty list if no claims found or error occurs
        return List.of();
    }
} 
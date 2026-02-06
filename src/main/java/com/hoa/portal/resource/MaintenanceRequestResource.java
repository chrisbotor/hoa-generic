package com.hoa.portal.resource;

import io.quarkus.security.Authenticated; // Add this import
import com.hoa.portal.entity.MaintenanceRequest;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;

@Path("/portal/requests")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MaintenanceRequestResource {

    @Inject
    SecurityIdentity identity;

    @GET
    @RolesAllowed({"admin", "resident"})
    public List<MaintenanceRequest> getRequests() {
        System.out.println("User: " + identity.getPrincipal().getName());
        System.out.println("Roles: " + identity.getRoles());
        // 1. If Admin, return every request in the system
        if (identity.getRoles().contains("admin")) {
            return MaintenanceRequest.listAll();
        }
        
        // 2. If Resident, extract their UUID from the JWT claims
        // Note: The claim name must match what we set in AuthResource
        Object claim = identity.getAttributes().get("https://hasura.io/jwt/claims");
        if (claim instanceof java.util.Map) {
            java.util.Map<String, Object> claims = (java.util.Map<String, Object>) claim;
            String userIdStr = (String) claims.get("x-hasura-user-id");
            return MaintenanceRequest.findByResident(UUID.fromString(userIdStr));
        }

        return List.of(); // Return empty if something is wrong with the token
    }
}
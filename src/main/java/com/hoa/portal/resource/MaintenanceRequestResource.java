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
    try {
        if (identity.getRoles().contains("admin")) {
            return MaintenanceRequest.listAll();
        }
        
        // Extract the claim safely
        Map<String, Object> claims = identity.getAttribute("https://hasura.io/jwt/claims");
        if (claims != null) {
            String userIdStr = (String) claims.get("x-hasura-user-id");
            return MaintenanceRequest.find("requesterId", UUID.fromString(userIdStr)).list();
        }
    } catch (Exception e) {
        // This will print the actual error to your 'docker logs'
        e.printStackTrace(); 
    }
    return List.of();
}
}
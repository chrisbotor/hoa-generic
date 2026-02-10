package com.hoa.portal.resource;

import com.hoa.portal.entity.MaintenanceRequest;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.json.JsonObject;
import java.util.List;

@Path("/tickets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MaintenanceResource {

    @Inject
    SecurityIdentity identity;

    @Inject
    JsonWebToken jwt;

    @GET
    @RolesAllowed({"admin", "resident"})
    public List<MaintenanceRequest> getTickets() {
        // Log the user for debugging
        System.out.println("DEBUG: Fetching tickets for user: " + identity.getPrincipal().getName());

        if (identity.getRoles().contains("admin")) {
            return MaintenanceRequest.listAll();
        }
        
        // Extract the house-id claim we set in AuthResource
        try {
            JsonObject claims = jwt.getClaim("https://hasura.io/jwt/claims");
            String houseIdStr = claims.getString("x-hasura-user-id"); // Using user-id or house-id depending on your mapping
            
            // If you specifically need x-hasura-house-id:
            if (claims.containsKey("x-hasura-house-id")) {
                houseIdStr = claims.getString("x-hasura-house-id");
            }

            System.out.println("DEBUG: Filtering for House ID: " + houseIdStr);
            
            Long myHouseId = Long.parseLong(houseIdStr);
            return MaintenanceRequest.find("houseId", myHouseId).list();
            
        } catch (Exception e) {
            System.out.println("DEBUG: Error parsing JWT claims: " + e.getMessage());
            return List.of();
        }
    }
}
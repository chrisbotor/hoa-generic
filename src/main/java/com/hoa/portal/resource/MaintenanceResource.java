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
        String username = identity.getPrincipal().getName();
        System.out.println("DEBUG: Fetching tickets for user: " + username);

        // 1. Check Roles
        System.out.println("DEBUG: User roles: " + identity.getRoles());

        if (identity.getRoles().contains("admin")) {
            System.out.println("DEBUG: Admin detected. Fetching ALL tickets.");
            return MaintenanceRequest.listAll();
        }
        
        // 2. Extract House ID from JWT
        try {
            JsonObject claims = jwt.getClaim("https://hasura.io/jwt/claims");
            if (claims == null) {
                System.out.println("DEBUG: FAILED - No Hasura claims found in JWT!");
                return List.of();
            }

            String houseIdStr = claims.getString("x-hasura-house-id");
            System.out.println("DEBUG: Extracted x-hasura-house-id from JWT: " + houseIdStr);

            if (houseIdStr == null || houseIdStr.equals("0")) {
                System.out.println("DEBUG: WARNING - House ID is 0 or null. No tickets will be found.");
                return List.of();
            }

            Long myHouseId = Long.parseLong(houseIdStr);
            
            // 3. Query Database
            List<MaintenanceRequest> tickets = MaintenanceRequest.find("houseId", myHouseId).list();
            System.out.println("DEBUG: Database returned " + tickets.size() + " tickets for house_id " + myHouseId);
            
            return tickets;

        } catch (Exception e) {
            System.out.println("DEBUG: ERROR during ticket retrieval: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
}
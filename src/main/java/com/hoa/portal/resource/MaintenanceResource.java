package com.hoa.portal.resource;

import com.hoa.portal.entity.MaintenanceRequest;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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
        // Admins see everything
        if (identity.getRoles().contains("admin")) {
            return MaintenanceRequest.listAll();
        }
        
        // Residents only see tickets linked to their house_id in the JWT
        JsonObject claims = jwt.getClaim("https://hasura.io/jwt/claims");
        String houseIdStr = claims.getString("x-hasura-house-id");
        
        // If the claim is missing or 0, return empty list to prevent errors
        if (houseIdStr == null || houseIdStr.equals("0")) {
            return List.of();
        }

        Long myHouseId = Long.parseLong(houseIdStr);
        return MaintenanceRequest.find("houseId", myHouseId).list();
    }

    @POST
    @RolesAllowed("resident")
    @Transactional
    public MaintenanceRequest createTicket(MaintenanceRequest request) {
        JsonObject claims = jwt.getClaim("https://hasura.io/jwt/claims");
        request.houseId = Long.parseLong(claims.getString("x-hasura-house-id"));
        
        if (request.status == null) {
            request.status = "OPEN";
        }
        
        request.persist();
        return request;
    }
}
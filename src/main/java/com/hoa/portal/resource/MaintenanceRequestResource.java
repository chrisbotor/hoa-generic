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
            JsonObject hasuraClaims = jwt.getClaim("https://hasura.io/jwt/claims");
            if (hasuraClaims != null) {
                String userIdStr = hasuraClaims.getString("x-hasura-user-id");
                return MaintenanceRequest.find("requesterId", UUID.fromString(userIdStr)).list();
            }
        } catch (Exception e) {
            System.err.println("Read Error: " + e.getMessage());
        }
        return List.of();
    }

    @POST
    @RolesAllowed({"admin", "resident"})
    @Transactional
    public MaintenanceRequest createRequest(MaintenanceRequest request) {
        JsonObject hasuraClaims = jwt.getClaim("https://hasura.io/jwt/claims");
        String userIdStr = hasuraClaims.getString("x-hasura-user-id");
        
        request.requesterId = UUID.fromString(userIdStr);
        request.status = "pending";
        request.persist();
        return request;
    }

    @PATCH
    @Path("/{id}/status")
    @RolesAllowed("admin")
    @Transactional
    public MaintenanceRequest updateStatus(@PathParam("id") Long id, MaintenanceRequest update) {
        MaintenanceRequest entity = MaintenanceRequest.findById(id);
        if (entity == null) throw new NotFoundException();
        entity.status = update.status;
        return entity;
    }
}

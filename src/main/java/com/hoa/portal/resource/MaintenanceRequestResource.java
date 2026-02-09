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
import java.time.LocalDateTime;

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
        // Admins see everything for management purposes
        if (identity.getRoles().contains("admin")) {
            return MaintenanceRequest.listAll();
        }
        
        try {
            JsonObject claims = jwt.getClaim("https://hasura.io/jwt/claims");
            int houseId = claims.getInt("x-hasura-house-id");

            // STRICT FILTER: Only show requests belonging to this resident's house
            // This includes requests made by the resident OR by the admin for this house
            return MaintenanceRequest.find("houseId", houseId).list();
            
        } catch (Exception e) {
            return List.of();
        }
    }

    @POST
    @RolesAllowed({"admin", "resident"})
    @Transactional
    public MaintenanceRequest createRequest(MaintenanceRequest request) {
        try {
            JsonObject claims = jwt.getClaim("https://hasura.io/jwt/claims");
            request.requesterId = UUID.fromString(claims.getString("x-hasura-user-id"));
            
            if (identity.getRoles().contains("admin")) {
                // If admin doesn't pick a house, default to 0 (Common Area)
                if (request.houseId == null) request.houseId = 0;
            } else {
                // Resident is forced to their own house ID
                request.houseId = claims.getInt("x-hasura-house-id");
            }
            
            request.status = "pending";
            request.createdAt = LocalDateTime.now();
            request.persist();
            return request;
        } catch (Exception e) {
            throw new WebApplicationException("Failed to create request", 400);
        }
    }

    @PATCH
    @Path("/{id}/status")
    @RolesAllowed("admin")
    @Transactional
    public MaintenanceRequest updateStatus(@PathParam("id") Long id, MaintenanceRequest updateData) {
        MaintenanceRequest entity = MaintenanceRequest.findById(id);
        if (entity == null) throw new NotFoundException();
        if (updateData.status != null) entity.status = updateData.status;
        return entity;
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("admin")
    @Transactional
    public void deleteRequest(@PathParam("id") Long id) {
        if (!MaintenanceRequest.deleteById(id)) throw new NotFoundException();
    }
}
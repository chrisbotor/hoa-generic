package com.hoa.portal.resource;

import com.hoa.portal.entity.House;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.json.JsonObject;
import java.util.List;

@Path("/houses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HouseResource {

    @Inject
    SecurityIdentity identity;

    @Inject
    JsonWebToken jwt;

    @GET
    @RolesAllowed({"admin", "resident"})
    public List<House> getHouses() {
        // Admins see all houses
        if (identity.getRoles().contains("admin")) {
            return House.listAll();
        }
        
        // Residents only see the house linked to their JWT claim
        JsonObject claims = jwt.getClaim("https://hasura.io/jwt/claims");
        // Note: Claim values are often strings in the JWT header
        String houseIdStr = claims.getString("x-hasura-house-id");
        Long myHouseId = Long.parseLong(houseIdStr);

        return House.find("id = ?1", myHouseId).list();
    }

    @POST
    @RolesAllowed("admin")
    @Transactional
    public House createHouse(House house) {
        house.persist();
        return house;
    }

    @PATCH
    @Path("/{id}")
    @RolesAllowed("admin")
    @Transactional
    public House updateHouse(@PathParam("id") Long id, House updateData) {
        House entity = House.findById(id);
        if (entity == null) {
            throw new NotFoundException("House not found");
        }
        
        // Updating based on your actual schema fields
        if (updateData.lotNumber != null) entity.lotNumber = updateData.lotNumber;
        if (updateData.streetAddress != null) entity.streetAddress = updateData.streetAddress;
        if (updateData.ownerId != null) entity.ownerId = updateData.ownerId;
        
        return entity;
    }
}
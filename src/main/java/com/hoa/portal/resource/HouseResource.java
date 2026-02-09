package com.hoa.portal.resource;

import com.hoa.portal.entity.House;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
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
    public List<House> getAvailableHouses() {
        if (identity.getRoles().contains("admin")) {
            return House.listAll();
        }

        // Residents can ONLY fetch their own house details
        JsonObject claims = jwt.getClaim("https://hasura.io/jwt/claims");
        int houseId = claims.getInt("x-hasura-house-id");
        return House.find("id", houseId).list();
    }
}
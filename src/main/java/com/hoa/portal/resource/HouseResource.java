package com.hoa.portal.resource;

import com.hoa.portal.entity.House;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/houses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HouseResource {

    @GET
    @RolesAllowed("admin") // Only admins need to see the full list
    public List<House> getAllHouses() {
        return House.listAll();
    }
}
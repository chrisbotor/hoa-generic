package com.hoa.portal.resource;

import com.hoa.portal.entity.MaintenanceRequest;
import jakarta.annotation.security.RolesAllowed; // Bring this back
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/tickets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MaintenanceResource {

    @GET
    @RolesAllowed({"admin", "resident"}) // Security is back on
    public List<MaintenanceRequest> getTickets() {
        return MaintenanceRequest.listAll();
    }
}
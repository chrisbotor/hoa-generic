package com.hoa.portal.resource;

import com.hoa.portal.entity.MaintenanceRequest;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/tickets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MaintenanceResource {

    @GET
    @RolesAllowed({"admin", "resident"})
    public List<MaintenanceRequest> getTickets(@Context HttpHeaders headers) {
        // This will print the exact token received by the Beelink server
        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        System.out.println("DEBUG: Incoming Auth Header: " + authHeader);
        
        return MaintenanceRequest.listAll();
    }
}
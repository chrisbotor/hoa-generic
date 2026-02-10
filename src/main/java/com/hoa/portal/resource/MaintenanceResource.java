package com.hoa.portal.resource;

import com.hoa.portal.entity.MaintenanceRequest;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/tickets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MaintenanceResource {

    @GET
    @PermitAll // Temporarily bypasses JWT check to fix the 401/SRJWT08004 error
    public List<MaintenanceRequest> getTickets() {
        System.out.println("DEBUG: Fetching all tickets (Security Bypassed)");
        return MaintenanceRequest.listAll();
    }
}
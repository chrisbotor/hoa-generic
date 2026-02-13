package com.hoa.portal.resource;

import com.hoa.portal.entity.MaintenanceRequest;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;

@Path("/tickets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MaintenanceResource {

    @GET
    @PermitAll // Temporarily allow all to see if the token is even parsed
    public List<MaintenanceRequest> getTickets(@Context HttpHeaders headers, @Context SecurityContext sec) {
        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        
        // This will log to your Docker console
        System.out.println("--- SECURITY DIAGNOSTIC ---");
        System.out.println("Incoming Header: " + (authHeader != null ? "PRESENT" : "MISSING"));
        System.out.println("User Principal: " + (sec.getUserPrincipal() != null ? sec.getUserPrincipal().getName() : "ANONYMOUS"));
        System.out.println("Is Secure: " + sec.isSecure());
        System.out.println("User has 'resident' role: " + sec.isUserInRole("resident"));
        System.out.println("---------------------------");
        
        return MaintenanceRequest.listAll();
    }
}
package com.hoa.portal.resource;

import com.hoa.portal.entity.User;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Optional;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    SecurityIdentity identity;

    @GET
    @Path("/me")
    @RolesAllowed({"admin", "resident"})
    public Response getCurrentUser() {
        // Find user by the email (upn) stored in the JWT
        String email = identity.getPrincipal().getName();
        Optional<User> user = User.find("email", email).firstResultOptional();

        if (user.isPresent()) {
            return Response.ok(user.get()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
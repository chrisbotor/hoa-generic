package com.hoa.portal.resource;
import com.hoa.portal.entity.User;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @GET
    @RolesAllowed("admin") // Only Admin can see the full list of users
    public List<User> getAllUsers() {
        return User.listAll();
    }

    @GET
    @Path("/{email}")
    public User getUserByEmail(@PathParam("email") String email) {
        return User.findByEmail(email);
    }
}
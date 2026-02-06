package com.yourname.hoa.resources;

import com.yourname.hoa.entity.User;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @GET
    public List<User> getAllUsers() {
        return User.listAll();
    }

    @GET
    @Path("/{email}")
    public User getUserByEmail(@PathParam("email") String email) {
        return User.findByEmail(email);
    }
}
package org.example.rest;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.dto.AuthResponse;
import org.example.dto.LoginRequest;
import org.example.ejb.UserEJB;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @EJB
    private UserEJB userEJB;

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        try {
            if (request == null || request.getUsername() == null || request.getPassword() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Имя пользователя и пароль обязательны")
                    .build();
            }

            var user = userEJB.authenticate(request.getUsername(), request.getPassword());
            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Неверное имя пользователя или пароль")
                    .build();
            }

            AuthResponse response = new AuthResponse(user.getId(), user.getUsername());
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Ошибка входа: " + e.getMessage())
                .build();
        }
    }

    @POST
    @Path("/register")
    public Response register(LoginRequest request) {
        try {
            if (request == null || request.getUsername() == null || request.getPassword() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Имя пользователя и пароль обязательны")
                    .build();
            }

            var user = userEJB.createUser(request.getUsername(), request.getPassword());
            AuthResponse response = new AuthResponse(user.getId(), user.getUsername());
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Ошибка регистрации: " + e.getMessage())
                .build();
        }
    }
}


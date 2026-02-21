package org.example.rest;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.dto.AuthResponse;
import org.example.dto.LoginRequest;
import org.example.ejb.UserEJB;
import org.example.entity.User;

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
                    .entity(new AuthResponse(false, "Необходимо указать имя пользователя и пароль"))
                    .build();
            }

            User user = userEJB.authenticate(request.getUsername(), request.getPassword());
            if (user != null) {
                return Response.ok(new AuthResponse(true, "Успешный вход", user.getId(), user.getUsername()))
                    .build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new AuthResponse(false, "Неверное имя пользователя или пароль"))
                    .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new AuthResponse(false, "Ошибка сервера: " + e.getMessage()))
                .build();
        }
    }

    @POST
    @Path("/register")
    public Response register(LoginRequest request) {
        try {
            if (request == null || request.getUsername() == null || request.getPassword() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new AuthResponse(false, "Необходимо указать имя пользователя и пароль"))
                    .build();
            }

            if (request.getUsername().trim().isEmpty() || request.getPassword().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new AuthResponse(false, "Имя пользователя и пароль не могут быть пустыми"))
                    .build();
            }

            User user = userEJB.createUser(request.getUsername(), request.getPassword());
            return Response.ok(new AuthResponse(true, "Пользователь успешно зарегистрирован", user.getId(), user.getUsername()))
                .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT)
                .entity(new AuthResponse(false, e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new AuthResponse(false, "Ошибка сервера: " + e.getMessage()))
                .build();
        }
    }
}


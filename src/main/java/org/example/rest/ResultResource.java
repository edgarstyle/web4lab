package org.example.rest;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.dto.PointRequest;
import org.example.dto.ResultDTO;
import org.example.ejb.ResultEJB;
import org.example.ejb.UserEJB;
import org.example.entity.Result;
import org.example.entity.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Path("/results")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ResultResource {

    @EJB
    private ResultEJB resultEJB;

    @EJB
    private UserEJB userEJB;

    @POST
    @Path("/check")
    public Response checkPoint(@QueryParam("userId") Long userId, PointRequest request) {
        try {
            if (userId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("userId обязателен")
                    .build();
            }

            if (request == null || request.getX() == null || request.getY() == null || request.getR() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("X, Y и R обязательны")
                    .build();
            }

            User user = userEJB.findById(userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Пользователь не найден")
                    .build();
            }

            BigDecimal x, y, r;
            try {
                x = new BigDecimal(request.getX());
                y = new BigDecimal(request.getY());
                r = new BigDecimal(request.getR());
            } catch (NumberFormatException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("X, Y и R должны быть числами")
                    .build();
            }

            if (r.compareTo(BigDecimal.ZERO) <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("R должен быть положительным числом")
                    .build();
            }

            Result result = resultEJB.checkPoint(x, y, r, user);
            ResultDTO dto = new ResultDTO(
                result.getId(),
                result.getX(),
                result.getY(),
                result.getR(),
                result.getHit(),
                result.getTimestamp(),
                result.getExecutionTime()
            );

            return Response.ok(dto).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Ошибка проверки точки: " + e.getMessage())
                .build();
        }
    }

    @GET
    public Response getResults(@QueryParam("userId") Long userId) {
        try {
            if (userId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("userId обязателен")
                    .build();
            }

            List<Result> results = resultEJB.getResultsByUser(userId);
            List<ResultDTO> dtos = results.stream()
                .map(r -> new ResultDTO(
                    r.getId(),
                    r.getX(),
                    r.getY(),
                    r.getR(),
                    r.getHit(),
                    r.getTimestamp(),
                    r.getExecutionTime()
                ))
                .collect(Collectors.toList());

            return Response.ok(dtos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Ошибка загрузки результатов: " + e.getMessage())
                .build();
        }
    }

    @DELETE
    public Response clearResults(@QueryParam("userId") Long userId) {
        try {
            if (userId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("userId обязателен")
                    .build();
            }

            resultEJB.clearResultsByUser(userId);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Ошибка очистки результатов: " + e.getMessage())
                .build();
        }
    }
}


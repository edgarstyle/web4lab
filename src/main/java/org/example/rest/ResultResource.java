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

    private ResultDTO toDTO(Result result) {
        return new ResultDTO(
            result.getId(),
            result.getX(),
            result.getY(),
            result.getR(),
            result.getHit(),
            result.getTimestamp(),
            result.getExecutionTime()
        );
    }

    @POST
    @Path("/check")
    public Response checkPoint(@QueryParam("userId") Long userId, PointRequest request) {
        try {
            if (userId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Необходима авторизация")
                    .build();
            }

            if (request == null || request.getX() == null || request.getY() == null || request.getR() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Необходимо указать координаты x, y и радиус r")
                    .build();
            }

            BigDecimal x = request.getX();
            BigDecimal y = request.getY();
            BigDecimal r = request.getR();

            // Проверяем только, что R положительный (для корректной работы AreaChecker)
            if (r.compareTo(BigDecimal.ZERO) <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("R должен быть положительным числом")
                    .build();
            }

            User user = userEJB.findById(userId);
            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Пользователь не найден")
                    .build();
            }

            Result result = resultEJB.checkPoint(x, y, r, user);
            return Response.ok(toDTO(result)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Ошибка сервера: " + e.getMessage())
                .build();
        }
    }

    @GET
    public Response getResults(@QueryParam("userId") Long userId) {
        try {
            if (userId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Необходима авторизация")
                    .build();
            }

            List<Result> results = resultEJB.getResultsByUser(userId);
            List<ResultDTO> resultDTOs = results.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
            
            return Response.ok(resultDTOs).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Ошибка сервера: " + e.getMessage())
                .build();
        }
    }

    @DELETE
    public Response clearResults(@QueryParam("userId") Long userId) {
        try {
            if (userId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Необходима авторизация")
                    .build();
            }

            resultEJB.clearResultsByUser(userId);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Ошибка сервера: " + e.getMessage())
                .build();
        }
    }
}


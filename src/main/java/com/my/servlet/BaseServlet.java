package com.my.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.configuration.JacksonConfig;
import com.my.dto.ApiResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

public abstract class BaseServlet extends HttpServlet {
    protected final ObjectMapper objectMapper = JacksonConfig.createObjectMapper();

    protected <T> T parseJson(HttpServletRequest req, Class<T> clazz) throws IOException {
        return objectMapper.readValue(req.getInputStream(), clazz);
    }

    protected void sendJson(HttpServletResponse resp, Object object) throws IOException {
        sendJson(resp, object, HttpServletResponse.SC_OK);
    }

    protected void sendJson(HttpServletResponse resp, Object object, int status) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(resp.getWriter(), object);
    }

    protected void sendError(HttpServletResponse resp, String message, int status) throws IOException {
        ApiResponse<?> response = ApiResponse.error(message);
        sendJson(resp, response, status);
    }

    protected Optional<Long> getId(HttpServletRequest req) {
        String reqId = req.getParameter("id");
        if (reqId == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(reqId));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}

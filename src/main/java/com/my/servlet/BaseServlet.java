package com.my.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.configuration.JacksonConfig;
import com.my.dto.ApiResponse;
import com.my.exception.EmptyBodyException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

public abstract class BaseServlet extends HttpServlet {
    protected final ObjectMapper objectMapper = JacksonConfig.createObjectMapper();

    protected <T> T parseJson(HttpServletRequest req, Class<T> clazz) throws IOException {
        validateRequestBody(req);
        return objectMapper.readValue(req.getInputStream(), clazz);
    }

    protected void validateRequestBody(HttpServletRequest req) {
        int contentLength = req.getContentLength();
        if (contentLength <= 0) {
            throw new EmptyBodyException("Тело запроса пустое");
        }
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

    protected void sendValidationError(HttpServletResponse resp, String message) throws IOException {
        sendError(resp, "Ошибка валидации: " + message, HttpServletResponse.SC_BAD_REQUEST);
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

    protected Long getLongParameter(HttpServletRequest req, String paramName) {
        String paramValue = req.getParameter(paramName);
        if (paramValue == null) {
            return null;
        }
        try {
            return Long.parseLong(paramValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected Double getDoubleParameter(HttpServletRequest req, String paramName) {
        String paramValue = req.getParameter(paramName);
        if (paramValue == null) {
            return null;
        }
        try {
            return Double.parseDouble(paramValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected Integer getIntegerParameter(HttpServletRequest req, String paramName) {
        String paramValue = req.getParameter(paramName);
        if (paramValue == null) {
            return null;
        }
        try {
            return Integer.parseInt(paramValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

package com.my.servlet;

import com.my.dto.ApiResponse;
import com.my.dto.UserRequestDto;
import com.my.dto.UserResponseDto;
import com.my.exception.AlreadyExistException;
import com.my.exception.EntityNotFoundException;
import com.my.mapper.UserMapper;
import com.my.model.User;
import com.my.service.UserService;
import com.my.service.impl.UserServiceImpl;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/auth")
public class AuthServlet extends BaseServlet {
    private final UserService userService;
    private final UserMapper userMapper;

    public AuthServlet() {
        this(new UserServiceImpl());
    }

    public AuthServlet(UserService userService) {
        this.userService = userService;
        this.userMapper = UserMapper.INSTANCE;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String action = req.getParameter("action");

            if ("register".equals(action)) {
                registerUser(req, resp);
            } else if ("login".equals(action)) {
                loginUser(req, resp);
            } else if ("logout".equals(action)) {
                logoutUser(resp);
            } else {
                sendError(resp, "Неизвестное действие. Доступные действия: register, login, logout",
                        HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (AlreadyExistException e) {
            sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            sendError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (Exception e) {
            sendError(resp, "Ошибка аутентификации: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void registerUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserRequestDto requestDto = parseJson(req, UserRequestDto.class);
        User registration = userService.registration(requestDto.email(), requestDto.username(), requestDto.password());
        UserResponseDto result = userMapper.toDto(registration);
        sendJson(resp, ApiResponse.success(result), HttpServletResponse.SC_CREATED);
    }

    private void loginUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserRequestDto requestDto = parseJson(req, UserRequestDto.class);
        User user = userService.login(requestDto.email(), requestDto.password());
        UserResponseDto result = userMapper.toDto(user);
        sendJson(resp, ApiResponse.success(result));
    }

    private void logoutUser(HttpServletResponse resp) throws IOException {
        userService.logout();
        sendJson(resp, ApiResponse.success("Выход выполнен успешно"));
    }
}

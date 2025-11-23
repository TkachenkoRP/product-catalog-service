package com.my.servlet;

import com.my.annotation.Loggable;
import com.my.dto.ApiResponse;
import com.my.dto.UserRequestDto;
import com.my.dto.UserResponseDto;
import com.my.exception.AlreadyExistException;
import com.my.exception.ArgumentNotValidException;
import com.my.exception.EmptyBodyException;
import com.my.exception.EntityNotFoundException;
import com.my.mapper.UserMapper;
import com.my.model.User;
import com.my.service.UserService;
import com.my.service.impl.UserServiceImpl;
import com.my.validation.Validation;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Loggable
@WebServlet("/user")
public class UserServlet extends BaseServlet {
    private final UserService userService;
    private final UserMapper userMapper;

    public UserServlet() {
        this(new UserServiceImpl());
    }

    public UserServlet(UserService userService) {
        this.userService = userService;
        this.userMapper = UserMapper.INSTANCE;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Optional<Long> optionalId = getId(req);
            if (optionalId.isEmpty()) {
                getAllUsers(resp);
                return;
            }
            long id = optionalId.get();
            getUserById(resp, id);
        } catch (EntityNotFoundException e) {
            sendError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (Exception e) {
            sendError(resp, "Ошибка получения пользователей: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void getAllUsers(HttpServletResponse resp) throws IOException {
        List<User> users = userService.getAll();
        List<UserResponseDto> responseDtos = userMapper.toDto(users);
        sendJson(resp, ApiResponse.success(responseDtos));
    }

    private void getUserById(HttpServletResponse resp, Long id) throws IOException {
        User user = userService.getById(id);
        UserResponseDto response = userMapper.toDto(user);
        sendJson(resp, ApiResponse.success(response));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Optional<Long> id = getId(req);
            if (id.isEmpty()) {
                sendError(resp, "Укажите ID пользователя", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            UserRequestDto requestDto = parseJson(req, UserRequestDto.class);

            Validation.validateUserUpdate(requestDto);

            User entity = userMapper.toEntity(requestDto);
            User updated = userService.update(id.get(), entity);
            UserResponseDto result = userMapper.toDto(updated);
            sendJson(resp, ApiResponse.success(result));
        } catch (ArgumentNotValidException e) {
            sendValidationError(resp, e.getMessage());
        } catch (EntityNotFoundException | EmptyBodyException | AlreadyExistException e) {
            sendError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (Exception e) {
            sendError(resp, "Ошибка обновления пользователя: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Optional<Long> id = getId(req);
            if (id.isEmpty()) {
                sendError(resp, "Укажите ID пользователя", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            boolean deleted = userService.delete(id.get());
            if (deleted) {
                sendJson(resp, ApiResponse.success("Пользователь успешно удален"));
            } else {
                sendError(resp, "Ошибка удаления пользователя",
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            sendError(resp, "Ошибка удаления пользователя: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}

package com.my.servlet;

import com.my.exception.EntityNotFoundException;
import com.my.model.User;
import com.my.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServletTest {
    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private UserServlet userServlet;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        userServlet = new UserServlet(userService);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    void doGet_WithoutId_ReturnsAllUsers() throws Exception {
        when(request.getParameter("id")).thenReturn(null);

        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@test.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@test.com");

        List<User> users = Arrays.asList(user1, user2);
        when(userService.getAll()).thenReturn(users);

        userServlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String jsonResponse = responseWriter.toString();
        assertThat(jsonResponse).contains("\"success\":true")
                .contains("user1@test.com")
                .contains("user2@test.com");
    }

    @Test
    void doGet_WithId_ReturnsUserById() throws Exception {
        when(request.getParameter("id")).thenReturn("1");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        when(userService.getById(1L)).thenReturn(user);

        userServlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String jsonResponse = responseWriter.toString();
        assertThat(jsonResponse).contains("\"success\":true")
                .contains("test@test.com");
    }

    @Test
    void doGet_UserNotFound_ReturnsNotFound() throws Exception {
        when(request.getParameter("id")).thenReturn("999");
        when(userService.getById(999L)).thenThrow(new EntityNotFoundException("User not found"));

        userServlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        String jsonResponse = responseWriter.toString();
        assertThat(jsonResponse).contains("\"success\":false")
                .contains("User not found");
    }

     @Test
    void doDelete_DeleteUser_Success() throws Exception {
        when(request.getParameter("id")).thenReturn("1");
        when(userService.delete(1L)).thenReturn(true);

        userServlet.doDelete(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String jsonResponse = responseWriter.toString();
        assertThat(jsonResponse).contains("\"success\":true")
                .contains("Пользователь успешно удален");
    }
}

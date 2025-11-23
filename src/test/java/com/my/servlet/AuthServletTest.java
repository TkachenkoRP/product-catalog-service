package com.my.servlet;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServletTest {
    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private AuthServlet authServlet;

    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        authServlet = new AuthServlet(userService);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    void doPost_LogoutUser_Success() throws Exception {
        when(request.getParameter("action")).thenReturn("logout");

        authServlet.doPost(request, response);

        verify(userService).logout();
        verify(response).setStatus(HttpServletResponse.SC_OK);
        String jsonResponse = responseWriter.toString();
        assertThat(jsonResponse).contains("\"success\":true");
        assertThat(jsonResponse).contains("Выход выполнен успешно");
    }

    @Test
    void doPost_UnknownAction_ReturnsError() throws Exception {
        when(request.getParameter("action")).thenReturn("unknown");

        authServlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String jsonResponse = responseWriter.toString();
        assertThat(jsonResponse).contains("\"success\":false");
        assertThat(jsonResponse).contains("Неизвестное действие");
    }
}

package com.my.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.dto.ApiResponseDto;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractControllerTest {
    protected MockMvc mockMvc;
    protected static final ObjectMapper objectMapper = new ObjectMapper();

    protected void setUpMockMvc(Object controller, ExceptionHandlerController exceptionHandlerController) {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionHandlerController)
                .build();
    }

    protected MockHttpServletResponse performRequest(HttpMethod method, String url, HttpStatus expectedStatus) throws Exception {
        return performRequest(method, url, null, expectedStatus, null);
    }

    protected MockHttpServletResponse performRequest(HttpMethod method, String url, Object content, HttpStatus expectedStatus) throws Exception {
        return performRequest(method, url, content, expectedStatus, null);
    }

    protected MockHttpServletResponse performRequest(HttpMethod method, String url, HttpStatus expectedStatus, Map<String, String> pathVariables) throws Exception {
        return performRequest(method, url, null, expectedStatus, pathVariables);
    }

    protected MockHttpServletResponse performRequest(HttpMethod method, String url, Object content, HttpStatus expectedStatus, Map<String, String> queryParams) throws Exception {
        MockHttpServletRequestBuilder requestBuilder;

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
        if (queryParams != null) {
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                uriBuilder.queryParam(entry.getKey(), entry.getValue());
            }
        }
        String finalUrl = uriBuilder.toUriString();

        if (method.equals(GET)) {
            requestBuilder = MockMvcRequestBuilders.get(finalUrl);
        } else if (method.equals(POST)) {
            requestBuilder = MockMvcRequestBuilders.post(finalUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(content));
        } else if (method.equals(PATCH)) {
            requestBuilder = MockMvcRequestBuilders.patch(finalUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(content));
        } else if (method.equals(DELETE)) {
            requestBuilder = MockMvcRequestBuilders.delete(finalUrl);
        } else if (method.equals(PUT)) {
            requestBuilder = MockMvcRequestBuilders.put(finalUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(content));
        } else {
            throw new IllegalArgumentException("Unsupported method: " + method);
        }

        MockHttpServletResponse response = mockMvc.perform(requestBuilder)
                .andExpect(status().is(expectedStatus.value()))
                .andReturn()
                .getResponse();
        response.setCharacterEncoding("UTF-8");
        return response;
    }

    protected static <T> T fromResponse(MockHttpServletResponse response, Class<T> clazz) throws JsonProcessingException, UnsupportedEncodingException {
        return objectMapper.readValue(response.getContentAsString(), clazz);
    }

    protected static <T> T fromResponse(MockHttpServletResponse response, TypeReference<T> typeReference) throws JsonProcessingException, UnsupportedEncodingException {
        return objectMapper.readValue(response.getContentAsString(), typeReference);
    }

    protected static <T> T extractDataFromResponse(MockHttpServletResponse response, Class<T> dataType) throws JsonProcessingException, UnsupportedEncodingException {
        ApiResponseDto<T> apiResponse = objectMapper.readValue(response.getContentAsString(),
                objectMapper.getTypeFactory().constructParametricType(ApiResponseDto.class, dataType));
        return apiResponse.data();
    }

    protected static <T> List<T> extractListFromResponse(MockHttpServletResponse response, Class<T> dataType) throws JsonProcessingException, UnsupportedEncodingException {
        JavaType listType = objectMapper.getTypeFactory().constructParametricType(List.class, dataType);
        JavaType apiResponseType = objectMapper.getTypeFactory().constructParametricType(ApiResponseDto.class, listType);

        ApiResponseDto<List<T>> apiResponse = objectMapper.readValue(response.getContentAsString(), apiResponseType);
        return apiResponse.data();
    }

    protected static String getResponseMessage(MockHttpServletResponse response) throws JsonProcessingException, UnsupportedEncodingException {
        ApiResponseDto<?> apiResponse = objectMapper.readValue(response.getContentAsString(), ApiResponseDto.class);
        return apiResponse.message();
    }
}

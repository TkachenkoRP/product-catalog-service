package com.my.controller;

import com.my.dto.ErrorResponseDto;
import com.my.exception.AlreadyExistException;
import com.my.exception.EntityHasReferencesException;
import com.my.exception.EntityNotFoundException;
import com.my.exception.ProductCreationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ErrorResponseDto notFound(EntityNotFoundException e) {
        return new ErrorResponseDto(e.getLocalizedMessage());
    }

    @ExceptionHandler(AlreadyExistException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorResponseDto alreadyExist(AlreadyExistException e) {
        return new ErrorResponseDto(e.getLocalizedMessage());
    }

    @ExceptionHandler(ProductCreationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorResponseDto productCreationException(ProductCreationException e) {
        return new ErrorResponseDto(e.getLocalizedMessage());
    }

    @ExceptionHandler(EntityHasReferencesException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public ErrorResponseDto entityHasReferences(EntityHasReferencesException e) {
        return new ErrorResponseDto(e.getLocalizedMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDto handleAllExceptions(Exception e) {
        return new ErrorResponseDto("Внутренняя ошибка сервера");
    }
}

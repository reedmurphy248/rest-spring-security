package com.security.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ProductDoesntExistException extends RuntimeException{

    public ProductDoesntExistException(String message) {
        super(message);
    }

}

package com.ing.brokerage.config;

import com.ing.brokerage.exception.BaseException;
import com.ing.brokerage.exception.BusinessException;
import com.ing.brokerage.exception.ErrorResponse;
import com.ing.brokerage.exception.NotAuthorizedException;
import com.ing.brokerage.exception.RecordNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    private final MessageSource customMessageResource;

    public CustomExceptionHandler(@Qualifier("customMessageResource") MessageSource customMessageResource) {

        this.customMessageResource = customMessageResource;
    }

    @ExceptionHandler({
        BusinessException.class,
        RecordNotFoundException.class,
        NotAuthorizedException.class,
        BaseException.class
    })
    public ResponseEntity<ErrorResponse> handleException(BaseException e, HttpServletRequest req) {

        String errorMessage = this.extractBaseExceptionMessage(e, req);
        log.error(errorMessage, e);
        return new ResponseEntity<>(new ErrorResponse(e.getMessageKey(), errorMessage), getHttpStatus(e));
    }

    private HttpStatus getHttpStatus(BaseException e) {

        if (e instanceof RecordNotFoundException) {
            return HttpStatus.NOT_FOUND;
        } else if (e instanceof NotAuthorizedException) {
            return HttpStatus.UNAUTHORIZED;
        }
        return HttpStatus.BAD_REQUEST;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpServletRequest req) {

        String message = StringUtils.join(
            e.getBindingResult()
             .getFieldErrors()
             .stream().map(error -> error.getField() + " " + error.getDefaultMessage())
             .toList(),
            ",");
        log.error(message, e);
        return new ResponseEntity<>(new ErrorResponse(message, "MA100"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest req) {

        String message = StringUtils.join(
            e.getConstraintViolations().stream().map(violation -> violation.getPropertyPath() + " " + violation.getMessage()).toList(),
            ",");
        log.error(message, e);
        return new ResponseEntity<>(new ErrorResponse(message, "CV100"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException e, HttpServletRequest req) {

        log.error(e.getMessage(), e);
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage("Unauthorized");
        errorResponse.setError("Unauthorized");
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException e, HttpServletRequest req) {

        log.error(e.getMessage(), e);
        return new ResponseEntity<>(new ErrorResponse("E101","Wrong credentials"), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleDefaultException(Exception e, HttpServletRequest req) {

        log.error(e.getMessage(), e);
        return new ResponseEntity<>(new ErrorResponse(), HttpStatus.BAD_REQUEST);
    }


    protected String extractBaseExceptionMessage(BaseException e, HttpServletRequest req) {

        if (StringUtils.isEmpty(e.getMessage())) {
            return this.customMessageResource.getMessage(e.getMessageKey(), e.getArgs(),
                                                         e.getMessageKey(), req.getLocale());
        } else {
            return e.getMessage();
        }
    }
}

package com.pccw.ruby.common.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pccw.ruby.common.constants.ErrorDescription;
import com.pccw.ruby.common.enums.ErrorCode;
import com.pccw.ruby.common.exception.DataDuplicationException;
import com.pccw.ruby.common.exception.DataNotFoundException;
import com.pccw.ruby.common.exception.response.ExceptionResponse;
import com.pccw.ruby.common.exception.response.ValidationExceptionObject;
import com.pccw.ruby.common.exception.response.ValidationExceptionResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
@Order
@ConditionalOnMissingBean(CommonResponseEntityExceptionHandler.class)
@Slf4j
public class CommonResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired protected ObjectMapper objectMapper;

    /**
     * Provides handling for common exceptions.
     *
     * @param ex the target exception
     * @param request the current request
     */
    @ExceptionHandler({
        IllegalArgumentException.class,
        DataNotFoundException.class,
        DataDuplicationException.class,
        HttpClientErrorException.class,
        HttpServerErrorException.class,
        FeignException.class,
        Exception.class
    })
    public final ResponseEntity<Object> handleCommonException(Exception ex, WebRequest request)
            throws Exception {
        HttpHeaders headers = new HttpHeaders();
        if (ex instanceof IllegalArgumentException) {
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return handleIllegalArgumentException(
                    (IllegalArgumentException) ex, headers, status, request);
        } else if (ex instanceof DataNotFoundException) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            return handleDataNotFoundException(
                    (DataNotFoundException) ex, headers, status, request);
        } else if (ex instanceof DataDuplicationException) {
            HttpStatus status = HttpStatus.CONFLICT;
            return handleDataDuplicationException(
                    (DataDuplicationException) ex, headers, status, request);
        } else if (ex instanceof FeignException) {
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return handleFeignException((FeignException) ex, headers, status, request);
        } else {
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return handleOtherExceptions(ex, headers, status, request);
        }
    }

    /**
     * Customize the response for BindException.
     *
     * <p>This method delegates to {@link #handleValidationExceptionInternal}.
     *
     * @param ex the exception
     * @param headers the headers to be written to the response
     * @param status the selected response status
     * @param request the current request
     * @return a {@code ResponseEntity} instance
     */
    @Override
    protected ResponseEntity<Object> handleBindException(
            BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        return handleValidationExceptionInternal(
                ErrorCode.VALIDATION_ERROR,
                ErrorDescription.VALIDATION_ERROR,
                ex.getBindingResult(),
                headers,
                status,
                request);
    }

    /**
     * Customize the response for MethodArgumentNotValidException.
     *
     * <p>This method delegates to {@link #handleValidationExceptionInternal}.
     *
     * @param ex the exception
     * @param headers the headers to be written to the response
     * @param status the selected response status
     * @param request the current request
     * @return a {@code ResponseEntity} instance
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        return handleValidationExceptionInternal(
                ErrorCode.VALIDATION_ERROR,
                ErrorDescription.VALIDATION_ERROR,
                ex.getBindingResult(),
                headers,
                status,
                request);
    }

    /**
     * Customize the response for IllegalArgumentException.
     *
     * <p>This method delegates to {@link #handleCommonExceptionInternal}.
     *
     * @param ex the exception
     * @param headers the headers to be written to the response
     * @param status the selected response status
     * @param request the current request
     * @return a {@code ResponseEntity} instance
     */
    protected ResponseEntity<Object> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        log.error(
                "IllegalArgumentException: {}, {}", ex.getMessage(), request.getDescription(false));

        return handleCommonExceptionInternal(
                ErrorCode.ILLEGAL_ARGUMENT, ex.getMessage(), headers, status, request);
    }

    /**
     * Customize the response for DataNotFoundException.
     *
     * <p>This method delegates to {@link #handleCommonExceptionInternal}.
     *
     * @param ex the exception
     * @param headers the headers to be written to the response
     * @param status the selected response status
     * @param request the current request
     * @return a {@code ResponseEntity} instance
     */
    protected ResponseEntity<Object> handleDataNotFoundException(
            DataNotFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("DataNotFoundException: {}, {}", ex.getMessage(), request.getDescription(false));

        return handleCommonExceptionInternal(
                ErrorCode.DATA_NOT_FOUND, ex.getMessage(), headers, status, request);
    }

    /**
     * Customize the response for DataDuplicationException.
     *
     * <p>This method delegates to {@link #handleCommonExceptionInternal}.
     *
     * @param ex the exception
     * @param headers the headers to be written to the response
     * @param status the selected response status
     * @param request the current request
     * @return a {@code ResponseEntity} instance
     */
    protected ResponseEntity<Object> handleDataDuplicationException(
            DataDuplicationException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        log.error(
                "DataDuplicationException: {}, {}", ex.getMessage(), request.getDescription(false));

        return handleCommonExceptionInternal(
                ErrorCode.DATA_DUPLICATION, ex.getMessage(), headers, status, request);
    }

    /**
     * Customize the response for FeignException.
     *
     * @param ex the exception
     * @param headers the headers to be written to the response
     * @param status the selected response status
     * @param request the current request
     * @return a {@code ResponseEntity} instance
     */
    protected ResponseEntity<Object> handleFeignException(
            FeignException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("FeignException: {}, {}", ex.getMessage(), request.getDescription(false));
        ExceptionResponse exceptionResponseDTO;

        try {
            exceptionResponseDTO =
                    objectMapper.readValue(ex.contentUTF8(), ExceptionResponse.class);
            exceptionResponseDTO.setStatus(status.value());
            exceptionResponseDTO.setCode(ErrorCode.FEIGN_ERROR.getCode());
        } catch (Exception e) {
            log.error(
                    "Cannot read error response: {}, {}",
                    e.getMessage(),
                    request.getDescription(false));
            exceptionResponseDTO =
                    new ExceptionResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            ErrorCode.UNKNOWN_ERROR.getCode(),
                            ErrorDescription.UNKNOWN_ERROR);
            return new ResponseEntity<>(
                    exceptionResponseDTO, headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(exceptionResponseDTO, headers, status);
    }

    /**
     * Customize the response for Exception.
     *
     * <p>This method delegates to {@link #handleCommonExceptionInternal}.
     *
     * @param ex the exception
     * @param headers the headers to be written to the response
     * @param status the selected response status
     * @param request the current request
     * @return a {@code ResponseEntity} instance
     */
    protected ResponseEntity<Object> handleOtherExceptions(
            Exception ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("Exception: {}, {}", ex.getMessage(), request.getDescription(false), ex);

        return handleCommonExceptionInternal(
                ErrorCode.UNKNOWN_ERROR, ErrorDescription.UNKNOWN_ERROR, headers, status, request);
    }

    /**
     * A single place to customize the response body of standard Spring MVC exception types.
     *
     * @param ex the exception
     * @param body the body for the response
     * @param headers the headers for the response
     * @param status the response status
     * @param request the current request
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error(
                "Standard Spring MVC exception: {}, {}",
                ex.getMessage(),
                request.getDescription(false));
        ExceptionResponse exceptionResponseDTO =
                new ExceptionResponse(
                        status.value(),
                        ErrorCode.UNKNOWN_ERROR.getCode(),
                        ErrorDescription.UNKNOWN_ERROR);

        return new ResponseEntity<>(exceptionResponseDTO, headers, status);
    }

    /**
     * A single place to customize the response body of validation exception types.
     *
     * @param bindingResult the bindingResult
     * @param headers the headers for the response
     * @param status the response status
     * @param request the current request
     */
    protected ResponseEntity<Object> handleValidationExceptionInternal(
            ErrorCode errorCode,
            String description,
            BindingResult bindingResult,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        log.error(
                "Field Validation Error, {}, {}",
                bindingResult.toString(),
                request.getDescription(false));
        ValidationExceptionResponse validationExceptionResponseDTO =
                new ValidationExceptionResponse(status.value(), errorCode.getCode(), description);
        if (null != bindingResult) {
            validationExceptionResponseDTO.setErrors(
                    bindingResult.getFieldErrors().stream()
                            .map(
                                    error ->
                                            new ValidationExceptionObject(
                                                    error.getField(), error.getDefaultMessage()))
                            .collect(Collectors.toList()));
        }

        return new ResponseEntity<>(validationExceptionResponseDTO, status);
    }

    /**
     * A single place to customize the response body of common exception types.
     *
     * @param description the error description
     * @param headers the headers for the response
     * @param status the response status
     * @param request the current request
     */
    protected ResponseEntity<Object> handleCommonExceptionInternal(
            ErrorCode errorCode,
            String description,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        ExceptionResponse exceptionResponseDTO =
                new ExceptionResponse(status.value(), errorCode.getCode(), description);

        return new ResponseEntity<>(exceptionResponseDTO, headers, status);
    }
}

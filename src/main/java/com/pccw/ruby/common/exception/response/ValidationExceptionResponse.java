package com.pccw.ruby.common.exception.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ValidationExceptionResponse extends ExceptionResponse {
    private List<ValidationExceptionObject> errors;

    public ValidationExceptionResponse(int status, String code, String description) {
        super(status, code, description);
    }

    public ValidationExceptionResponse(
            int status, String code, String description, List<ValidationExceptionObject> errors) {
        super(status, code, description);
        this.errors = errors;
    }
}

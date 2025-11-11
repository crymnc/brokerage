package com.ing.brokerage.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ErrorResponse {

    private String error = "E100";

    private String message = "Operation is failed. Please try again.";

    /**
     * Constructor.
     */
    public ErrorResponse(String error, String message) {

        this.error = error;
        this.message = message;
    }
}

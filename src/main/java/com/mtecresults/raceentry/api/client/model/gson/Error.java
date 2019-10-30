package com.mtecresults.raceentry.api.client.model.gson;

import lombok.Data;

@Data
public class Error {
    ErrorMessage error;

    @Data
    public class ErrorMessage {
        long error_code;
        String error_msg;
    }
}

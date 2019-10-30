package com.mtecresults.raceentry.api.client.model.gson;

import lombok.Data;

import java.util.List;

@Data
public class LoginResponse {
    String tmp_key;
    String tmp_secret;
    User user;
}

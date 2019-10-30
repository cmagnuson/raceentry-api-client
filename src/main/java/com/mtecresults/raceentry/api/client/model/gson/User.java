package com.mtecresults.raceentry.api.client.model.gson;

import lombok.Data;

@Data
public class User {
    UserUser user;

    class UserUser {
        String first_name;
        String last_name;
    }
}

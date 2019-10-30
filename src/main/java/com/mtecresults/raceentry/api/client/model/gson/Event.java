package com.mtecresults.raceentry.api.client.model.gson;

import lombok.Data;

import java.util.Date;

@Data
public class Event {
    long event_id;
    String name;
    String event_type;
    String url;
    String description;
    Date last_modified;
    String contact_first_name;
    String contact_last_name;
    String contact_email;
    Date event_date;
    String street;
    String city;
    String state;
    String province;
    String country;
    String zipcode;
}

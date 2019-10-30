package com.mtecresults.raceentry.api.client.model.export;

import com.mtecresults.raceentry.api.client.model.gson.Participant;
import lombok.Data;

import java.util.function.Function;

@Data
public class StandardColumn implements Column {
    private final String name;
    private final Function<Participant, String> accessor;
}

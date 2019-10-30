package com.mtecresults.raceentry.api.client.model.export;

import com.mtecresults.raceentry.api.client.model.gson.Participant;

import java.util.function.Function;

public interface Column {
    String getName();
    Function<Participant, String> getAccessor();
}

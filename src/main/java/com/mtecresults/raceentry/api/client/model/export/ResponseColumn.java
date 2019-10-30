package com.mtecresults.raceentry.api.client.model.export;

import com.mtecresults.raceentry.api.client.model.gson.Participant;
import lombok.Data;

import java.util.function.Function;

@Data
public class ResponseColumn implements Column {
    private final String name;
    private final String responseId;


    public Function<Participant, String> getAccessor() {
        return participant -> {
            for (Participant.QuestionResponse questionResponse : participant.getResponses()) {
                if (questionResponse.getQid().equals(responseId)) {
                    return questionResponse.getAns();
                }
            }
            return "";
        };
    }
}
package com.mtecresults.raceentry.api.client.model.gson;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@Slf4j
public class GetCreateData {
    List<EventType> event_types;
    List<TimeZone> timezones;
    List<State> states;

    public class EventType {
      int id;
      String name;
    }

    public class TimeZone {
        int id;
        String name;
    }

    public class State {
        int id;
        String abbr;
        String name;
    }

    public String lookupStateAbbreviation(final String fullStateName){
        if(states != null){
            for(State state: states){
                if(state.name.equals(fullStateName)){
                    return state.abbr;
                }
            }
        }
        if(fullStateName.equals("NA")){
            //undocumented NA option
            return "";
        }
        log.warn("Unable to find abbreviation for state name: " + fullStateName);
        return fullStateName;
    }
}

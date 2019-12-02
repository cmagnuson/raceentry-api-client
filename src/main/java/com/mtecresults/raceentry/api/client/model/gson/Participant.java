package com.mtecresults.raceentry.api.client.model.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mtecresults.raceentry.api.client.controller.RaceEntryConnector;
import com.mtecresults.raceentry.api.client.model.export.Column;
import com.mtecresults.raceentry.api.client.model.export.ResponseColumn;
import com.mtecresults.raceentry.api.client.model.export.StandardColumn;
import com.mtecresults.raceentry.api.client.util.TimeFormats;
import lombok.Data;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Data
public class Participant {
    long participant_id;
    long category_id;
    String category_name;
    Long team_id;
    String first_name;
    String last_name;
    String email;
    String address;
    String city;
    String bib_num;
    String province;
    String state;
    String zipcode;
    Date dob;
    String gender;
    @JsonAdapter(value = LastModifiedTypeAdaptor.class)
    Long last_modified;
    String phone;
    String checked_in;
    List<QuestionResponse> responses;

    @Data
    public class QuestionResponse {
        String qid;
        String q;
        String qoid;
        String ans;
    }

    public List<ResponseColumn> getResponseColumns() {
        List<ResponseColumn> columns = new ArrayList<>();
        if(responses != null){
            for (QuestionResponse qr : responses) {
                ResponseColumn rc = new ResponseColumn(qr.q, qr.qid);
                columns.add(rc);
            }
        }
        return columns;
    }


    public static List<Column> getDefaultColumns() {
        Column[] defaultColumns = new Column[]{
                new StandardColumn("participantId", participant -> "" + participant.getParticipant_id()),
                new StandardColumn("categoryId", participant -> "" + participant.getCategory_id()),
                new StandardColumn("categoryName", participant -> "" + participant.getCategory_name()),
                new StandardColumn("teamId", participant -> "" + participant.getTeam_id()),
                new StandardColumn("firstName", participant -> "" + participant.getFirst_name()),
                new StandardColumn("lastName", participant -> "" + participant.getLast_name()),
                new StandardColumn("email", participant -> "" + participant.getEmail()),
                new StandardColumn("address", participant -> "" + participant.getAddress()),
                new StandardColumn("city", participant -> "" + participant.getCity()),
                new StandardColumn("bib", participant -> "" + participant.getBib_num()),
                new StandardColumn("province", participant -> "" + participant.getProvince()),
                new StandardColumn("state", participant -> "" + participant.getState()),
                new StandardColumn("zipcode", participant -> "" + participant.getZipcode()),
                new StandardColumn("dob", participant -> "" + TimeFormats.dateToFormattedMMDDYYYY(participant.getDob())),
                new StandardColumn("sex", participant -> "" + participant.getGender()),
                new StandardColumn("lastModified", participant -> "" + TimeFormats.timestampToFormatted(new Date(participant.getLast_modified()))),
                new StandardColumn("phone", participant -> "" + participant.getPhone()),
                new StandardColumn("checkedIn", participant -> "" + participant.getChecked_in()),
        };
        return Arrays.asList(defaultColumns);
    }

    public class LastModifiedTypeAdaptor extends TypeAdapter<Long> {

        @Override
        public void write(JsonWriter out, Long value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.value(value);
        }

        @Override
        public Long read(JsonReader in) throws IOException {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(RaceEntryConnector.DATE_FORMAT);
            if(in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }

            String timeStr = in.nextString();
            try {
                if (timeStr != null) {
                    Date d = simpleDateFormat.parse(timeStr);
                    return d.getTime();
                }
            } catch (ParseException pe) {
                throw new IOException("Can't parse date/time: "+timeStr, pe);
            }
            return null;
        }
    }
}

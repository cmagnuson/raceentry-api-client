package com.mtecresults.raceentry.api.client.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mtecresults.raceentry.api.client.model.ErrorWithRawJson;
import com.mtecresults.raceentry.api.client.model.RaceEntryCredentials;
import com.mtecresults.raceentry.api.client.model.gson.Event;
import com.mtecresults.raceentry.api.client.model.gson.LoginResponse;
import com.mtecresults.raceentry.api.client.model.gson.Participant;
import com.mtecresults.raceentry.api.client.model.ApiCredentials;
import com.spencerwi.either.Either;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.lang.Error;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Data
public class RaceEntryConnector {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String BIRTHDATE_FORMAT = "yyyy-MM-dd";

    private final OkHttpClient client;
    private final RaceEntryCredentials creds;

    public Either<ErrorWithRawJson, ApiCredentials> login() {
        HttpUrl.Builder urlBuilder = getLoginUrlBuilder(creds);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            final Gson gson = new GsonBuilder().create();
            Either<ErrorWithRawJson, LoginResponse> loginResponse = tryParse(gson, response, LoginResponse.class);
            return loginResponse.map(errorWithRawJson -> errorWithRawJson, loginResponse1 -> new ApiCredentials(loginResponse1.getTmp_key(), loginResponse1.getTmp_secret()));
        }
        catch (IOException e){
            return Either.left(new ErrorWithRawJson(null, null, request.url().toString(), e));
        }
    }

    public Either<ErrorWithRawJson, List<Participant>> getParticipants(ApiCredentials apiCredentials, long eventId) {
        HttpUrl.Builder urlBuilder = getEventParticipantsUrlBuilder(apiCredentials, eventId);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            Either<ErrorWithRawJson, Participant[]> participantsQueryResponseE;

            final Gson gson = new GsonBuilder().setDateFormat(BIRTHDATE_FORMAT).create();
            participantsQueryResponseE = tryParse(gson, response, Participant[].class);

            return participantsQueryResponseE.map(errorWithRawJson -> errorWithRawJson, participants -> Arrays.asList(participants));
        }
        catch (IOException e){
            return Either.left(new ErrorWithRawJson(null, null, request.url().toString(), e));
        }
    }

    public Either<ErrorWithRawJson, List<Event>> getEvents(ApiCredentials apiCredentials) {
        HttpUrl.Builder urlBuilder = getEventsUrlBuilder(apiCredentials);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            Either<ErrorWithRawJson, Event[]> eventsQueryResponseE;

            final Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
            eventsQueryResponseE = tryParse(gson, response, Event[].class);

            return eventsQueryResponseE.map(errorWithRawJson -> errorWithRawJson, events -> Arrays.asList(events));
        }
        catch (IOException e){
            return Either.left(new ErrorWithRawJson(null, null, request.url().toString(), e));
        }
    }

    private static <T> Either<ErrorWithRawJson, T> tryParse(Gson gson, Response response, Class<T> tClass) throws IOException {
        String body = response.body().string();

        try {
            return Either.right(gson.fromJson(body, tClass));
        }
        catch(JsonParseException jse){
            log.warn("Error parsing expected response: ", jse);
            //try and parse as error code
            try{
                Error error = gson.fromJson(body, Error.class);
                return Either.left(new ErrorWithRawJson(error, body, response.request().url().toString(), null));
            }
            catch(JsonParseException jse2){
                return Either.left(new ErrorWithRawJson(null, body, response.request().url().toString(), null));
            }
        }
    }

    private static HttpUrl.Builder getLoginUrlBuilder(RaceEntryCredentials creds){
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://www.raceentry.com/softwareapi/login").newBuilder();
        urlBuilder.addQueryParameter("email", creds.getUsername());
        urlBuilder.addQueryParameter("password", creds.getPassword());
        return urlBuilder;
    }

    private static HttpUrl.Builder getEventParticipantsUrlBuilder(ApiCredentials creds, Long eventId){
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://www.raceentry.com/softwareapi/get_race_participants").newBuilder();
        urlBuilder.addQueryParameter("tmp_key", creds.getKey());
        urlBuilder.addQueryParameter("tmp_secret", creds.getSecret());
        urlBuilder.addQueryParameter("event_id", eventId.toString());
        urlBuilder.addQueryParameter("get_responses", "yes");
        return urlBuilder;
    }

    private static HttpUrl.Builder getEventsUrlBuilder(ApiCredentials creds){
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://www.raceentry.com/softwareapi/get_races").newBuilder();
        urlBuilder.addQueryParameter("tmp_key", creds.getKey());
        urlBuilder.addQueryParameter("tmp_secret", creds.getSecret());
        return urlBuilder;
    }
}

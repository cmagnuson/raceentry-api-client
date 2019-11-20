package com.mtecresults.raceentry.api.client.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mtecresults.raceentry.api.client.model.ErrorWithRawJson;
import com.mtecresults.raceentry.api.client.model.RaceEntryCredentials;
import com.mtecresults.raceentry.api.client.model.gson.Event;
import com.mtecresults.raceentry.api.client.model.gson.GetCreateData;
import com.mtecresults.raceentry.api.client.model.gson.LoginResponse;
import com.mtecresults.raceentry.api.client.model.gson.Participant;
import com.mtecresults.raceentry.api.client.model.ApiCredentials;
import com.spencerwi.either.Either;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.lang.Error;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Data
public class RaceEntryConnector {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String BIRTHDATE_FORMAT = "yyyy-MM-dd";
    public static final String LOGIN_FAILED_MESSAGE = "E-mail and Password Don't Match";

    private final OkHttpClient client;
    private final RaceEntryCredentials creds;

    public Either<ErrorWithRawJson, ApiCredentials> login() {
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://www.raceentry.com/softwareapi/login").newBuilder();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("email", creds.getUsername())
                .addFormDataPart("password", creds.getPassword())
                .build();

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            final Gson gson = new GsonBuilder().create();
            Either<ErrorWithRawJson, LoginResponse> loginResponse = tryParse(gson, response, LoginResponse.class);
            if(loginResponse.isRight() && loginResponse.getRight().getMessage() != null && loginResponse.getRight().getMessage().equals(LOGIN_FAILED_MESSAGE)){
                return Either.left(new ErrorWithRawJson(null, loginResponse.getRight().toString(), request.url().toString(), new Exception("Login Credentials Not Valid")));
            }
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
            final Gson gson = new GsonBuilder().setDateFormat(BIRTHDATE_FORMAT).create();
            return tryParse(gson, response, Participant[].class).map(errorWithRawJson -> errorWithRawJson, Arrays::asList);
        }
        catch (IOException e){
            return Either.left(new ErrorWithRawJson(null, null, request.url().toString(), e));
        }
    }

    public Either<ErrorWithRawJson, GetCreateData> getCreateData(final ApiCredentials apiCredentials) {
        HttpUrl.Builder urlBuilder = getGetCreateDataUrlBuilder(apiCredentials);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            final Gson gson = new GsonBuilder().create();
            return tryParse(gson, response, GetCreateData.class);
        }
        catch (IOException e){
            return Either.left(new ErrorWithRawJson(null, null, request.url().toString(), e));
        }
    }

    public Either<ErrorWithRawJson, List<Event>> getEvents(final ApiCredentials apiCredentials) {
        HttpUrl.Builder urlBuilder = getEventsUrlBuilder(apiCredentials);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            final Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
            return tryParse(gson, response, Event[].class).map(errorWithRawJson -> errorWithRawJson, Arrays::asList);
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

    private static HttpUrl.Builder getEventParticipantsUrlBuilder(final ApiCredentials creds, Long eventId){
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://www.raceentry.com/softwareapi/get_race_participants").newBuilder();
        urlBuilder.addQueryParameter("tmp_key", creds.getKey());
        urlBuilder.addQueryParameter("tmp_secret", creds.getSecret());
        urlBuilder.addQueryParameter("event_id", eventId.toString());
        urlBuilder.addQueryParameter("get_responses", "yes");
        return urlBuilder;
    }

    private static HttpUrl.Builder getEventsUrlBuilder(final ApiCredentials creds){
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://www.raceentry.com/softwareapi/get_races").newBuilder();
        urlBuilder.addQueryParameter("tmp_key", creds.getKey());
        urlBuilder.addQueryParameter("tmp_secret", creds.getSecret());
        return urlBuilder;
    }

    private static HttpUrl.Builder getGetCreateDataUrlBuilder(final ApiCredentials creds){
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://www.raceentry.com/softwareapi/race/get_create_data").newBuilder();
        urlBuilder.addQueryParameter("tmp_key", creds.getKey());
        urlBuilder.addQueryParameter("tmp_secret", creds.getSecret());
        return urlBuilder;
    }
}

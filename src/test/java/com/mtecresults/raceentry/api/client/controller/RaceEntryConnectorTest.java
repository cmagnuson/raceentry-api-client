package com.mtecresults.raceentry.api.client.controller;

import com.mtecresults.raceentry.api.client.model.ApiCredentials;
import com.mtecresults.raceentry.api.client.model.RaceEntryCredentials;
import com.mtecresults.raceentry.api.client.model.ErrorWithRawJson;
import com.mtecresults.raceentry.api.client.model.gson.Event;
import com.mtecresults.raceentry.api.client.model.gson.GetCreateData;
import com.mtecresults.raceentry.api.client.model.gson.Participant;
import com.spencerwi.either.Either;
import okhttp3.OkHttpClient;
import okhttp3.mock.MockInterceptor;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static okhttp3.mock.ClasspathResources.resource;
import static okhttp3.mock.MediaTypes.MEDIATYPE_JSON;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class RaceEntryConnectorTest {

    private final RaceEntryCredentials credentials = new RaceEntryCredentials("user", "pass");
    private final long eventId = 1;

    @Test
    public void login() {
        MockInterceptor interceptor = new MockInterceptor();

        interceptor.addRule()
                .post()
                .respond(resource("login.json"), MEDIATYPE_JSON);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        RaceEntryConnector connector = new RaceEntryConnector(client, credentials);
        Either<ErrorWithRawJson, ApiCredentials> race = connector.login();
        assertTrue(race.isRight());
        assertNotNull(race.getRight().getKey());
        assertNotNull(race.getRight().getSecret());
    }

    @Test
    public void getParticipants() {
        MockInterceptor interceptor = new MockInterceptor();

        interceptor.addRule()
                .get()
                .anyTimes()
                .respond(resource("participants.json"), MEDIATYPE_JSON);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        RaceEntryConnector connector = new RaceEntryConnector(client, credentials);
        Either<ErrorWithRawJson, List<Participant>> participants = connector.getParticipants(new ApiCredentials("key", "secret"), eventId);
        assertTrue(participants.isRight());
        assertEquals(2, participants.getRight().size());
    }

    @Test
    public void getEvents() {
        MockInterceptor interceptor = new MockInterceptor();

        interceptor.addRule()
                .get()
                .anyTimes()
                .respond(resource("events.json"), MEDIATYPE_JSON);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        RaceEntryConnector connector = new RaceEntryConnector(client, credentials);
        Either<ErrorWithRawJson, List<Event>> events = connector.getEvents(new ApiCredentials("key", "secret"));
        assertTrue(events.isRight());
        assertEquals(2, events.getRight().size());
    }

    @Test
    public void getCreateData() {
        MockInterceptor interceptor = new MockInterceptor();

        interceptor.addRule()
                .get()
                .anyTimes()
                .respond(resource("get_create_data.json"), MEDIATYPE_JSON);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        RaceEntryConnector connector = new RaceEntryConnector(client, credentials);
        Either<ErrorWithRawJson, GetCreateData> getCreateData = connector.getCreateData(new ApiCredentials("key", "secret"));
        assertTrue(getCreateData.isRight());
        assertEquals(getCreateData.getRight().lookupStateAbbreviation("Wisconsin"), "WI");
    }
}
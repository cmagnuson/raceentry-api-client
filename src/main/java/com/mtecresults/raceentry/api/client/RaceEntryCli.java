package com.mtecresults.raceentry.api.client;

import com.mtecresults.raceentry.api.client.controller.CsvExporter;
import com.mtecresults.raceentry.api.client.controller.ManifestVersionProvider;
import com.mtecresults.raceentry.api.client.controller.RaceEntryConnector;
import com.mtecresults.raceentry.api.client.model.RaceEntryCredentials;
import com.mtecresults.raceentry.api.client.model.ErrorWithRawJson;
import com.mtecresults.raceentry.api.client.model.gson.Event;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@CommandLine.Command(versionProvider = ManifestVersionProvider.class,
        headerHeading = "Usage:%n",
        synopsisHeading = "",
        descriptionHeading = "%nDescription:%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        header = "Download RaceEntry registrations via command line",
        description = "Use your provided username, password and event id to download entries.%n%n" +
                "You can include arguments (username/password in particular) in a file and call with @FILE_PATH to include in command, for easier reuse between events.")
public class RaceEntryCli implements Callable<Void> {

    @CommandLine.Option(names = { "-h", "--help" }, usageHelp = true, description = "usage help")
    private boolean helpRequested;

    @CommandLine.Option(names = {"-V", "--version"}, versionHelp = true, description = "print version info")
    boolean versionRequested;

    @CommandLine.Option(names = {"-v", "--verbose"}, description = "verbose")
    boolean verbose;

    @CommandLine.Option(names = {"-l", "--list"}, description = "list events")
    private boolean listEvents = false;

    @CommandLine.Option(names = {"-e", "--event"}, description = "event id")
    private Long eventId;

    @CommandLine.Option(names = {"-u", "--username"}, required=true, description = "username")
    private String username;

    @CommandLine.Option(names = {"-p", "--password"}, required=true, description = "password")
    private String password;

    @CommandLine.Option(names = {"-f", "--file"}, description = "file to export to")
    private File exportFile;

    @CommandLine.Option(names = {"-o", "--overwrite"}, description = "overwrite existing file")
    private boolean overwriteExisting = false;

    public static void main(String[] args) {
        CommandLine.call(new RaceEntryCli(), args);
    }

    @Override
    public Void call() {
        OkHttpClient client = new OkHttpClient();
        RaceEntryCredentials credentials = new RaceEntryCredentials(username, password);

        RaceEntryConnector connector = new RaceEntryConnector(client, credentials);

        connector.login().run(RaceEntryCli::handleError, apiCredentials -> {
            if(verbose) {
                log.info("Received temporary credentials: " + apiCredentials);
            }
            if(listEvents){
                connector.getEvents(apiCredentials).run(RaceEntryCli::handleError, events -> {
                    log.info("Found Events (id, name, date):");
                    for(Event e: events){
                        log.info(e.getEvent_id()+" "+e.getName()+" "+e.getEvent_date());
                    }
                });
            } else if(eventId != null) {
                connector.getParticipants(apiCredentials, eventId).run(RaceEntryCli::handleError, participants -> {
                    log.info("Participants: " + participants.size());
                    final AtomicLong lastModified = new AtomicLong(1);
                    participants.forEach(p -> {
                        if (p.getLast_modified() > lastModified.get()) {
                            lastModified.set(p.getLast_modified());
                        }
                    });
                    log.info("Last Modified Time: " + lastModified);
                    if (verbose) {
                        participants.forEach(participant -> {
                            log.info("\t" + participant);
                        });
                    }
                    CsvExporter exporter = new CsvExporter(participants);
                    if (exportFile != null) {
                        boolean success = exporter.exportToFile(exportFile, overwriteExisting);
                        if (success) {
                            log.info("Download written to file: " + exportFile.getAbsolutePath());
                        }
                    }
                });
            }
        });

        return null;
    }

    private static void handleError(ErrorWithRawJson e){
        log.error("Error making API call: "+e);
    }
}



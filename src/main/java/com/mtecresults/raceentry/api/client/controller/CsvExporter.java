package com.mtecresults.raceentry.api.client.controller;

import com.mtecresults.raceentry.api.client.model.export.Column;
import com.mtecresults.raceentry.api.client.model.export.ResponseColumn;
import com.mtecresults.raceentry.api.client.model.gson.Participant;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Slf4j
public class CsvExporter {

    private final List<Participant> participants;

    public boolean exportToFile(File f, boolean overwriteIfExisting) {
        if(f.exists()){
            if(overwriteIfExisting){
                f.delete();
            }
            else {
                log.error("Unable to write to file: " + f.getAbsolutePath() + " it already exists");
                return false;
            }
        }
        try (CsvListWriter writer = new CsvListWriter(new BufferedWriter(new FileWriter(f)),CsvPreference.STANDARD_PREFERENCE)) {
            List<Column> columns = getColumns(participants);
            writer.write(columns.stream().map(Column::getName).collect(Collectors.toList()));
            for(Participant p: participants){
                writer.write(getRow(columns, p));
            }
            return true;
        }
        catch (IOException io){
            log.error("Error writing to file", io);
            return false;
        }
    }

    protected List<Column> getColumns(List<Participant> participants){
        List<Column> columns = getDefaultColumns();
        Map<String, ResponseColumn> rColumn = new HashMap<>();
        for(Participant p: participants){
            //scan participant QuestionResponses to get all unique columns
            for(ResponseColumn rc: p.getResponseColumns()){
                rColumn.put(rc.getResponseId(), rc);
            }
        }
        columns.addAll(rColumn.values());
        return columns;
    }

    protected List<String> getRow(List<Column> columns, Participant p){
        return columns.stream().map(c -> {
            try {
                String value = c.getAccessor().apply(p);
                return value == null ? "" : value;
            } catch (NullPointerException npe) {
                //this field is non-existant for this participant
                //just put a blank
                return "";
            }
        }).collect(Collectors.toList());
    }

    protected List<Column> getDefaultColumns() {
        List<Column> defaultColumns = new ArrayList<>();
        defaultColumns.addAll(Participant.getDefaultColumns());
        return defaultColumns;
    }
}

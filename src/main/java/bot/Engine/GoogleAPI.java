package bot.Engine;

import bot.Events;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.util.List;
import java.util.Collections;
import java.util.TreeMap;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * @author  Wil Aquino
 * Date:    April 12, 2021
 * Project: LaunchPoint Bot
 * Module:  GoogleAPI.java
 * Purpose: Establishes a connection with a Google Sheet
 *          through the Google API.
 */
public class GoogleAPI {

    /** Field for Google Sheets SDK. */
    private final Sheets sheetsService;

    /** Name of the application. */
    private static final String APPLICATION_NAME = "LaunchPoint Simp";

    /** ID of the Google Sheet being used. */
    private final String spreadsheetID;

    /**
     * Constructs a connection with a spreadsheet based on a provided
     * Google Sheet's ID.
     * @param id the ID of the Google Sheet.
     * @throws IOException ...
     * @throws GeneralSecurityException ...
     */
    public GoogleAPI(String id) throws IOException, GeneralSecurityException {
        sheetsService = getSheetsService();
        spreadsheetID = id;
    }

    /**
     * Creates an OAuth exchange to grant application access to Google Sheets.
     * @return the authorization credential.
     * @throws IOException ...
     * @throws GeneralSecurityException ...
     * @source Twilio on YouTube.
     */
    private Credential authorize()
            throws IOException, GeneralSecurityException {
        InputStream in = Graduate.class.getResourceAsStream(
                "/credentials.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets
                .load(JacksonFactory.getDefaultInstance(), new InputStreamReader(in));

        List<String> scopes = Collections.singletonList(SheetsScopes.SPREADSHEETS);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
                .Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
                clientSecrets, scopes)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
                .setAccessType("offline")
                .build();

        AuthorizationCodeInstalledApp oAuth = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver());

        return oAuth.authorize("user");
    }

    /**
     * Constructs the Google Sheets service link.
     * @return the service link.
     * @throws IOException ...
     * @throws GeneralSecurityException ...
     */
    private Sheets getSheetsService()
            throws IOException, GeneralSecurityException {
        Credential credential = authorize();
        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential).setApplicationName(
                APPLICATION_NAME).build();
    }

    /**
     * Retrieves the affiliated spreadsheet.
     * @return said spreadsheet.
     */
    public Sheets getSheet() {
        return sheetsService;
    }

    /**
     * Retrieves the affiliated spreadsheet's ID.
     * @return said ID.
     */
    public String getSpreadsheetID() {
        return spreadsheetID;
    }

    /**
     * Retrieves a table section of the spreadsheet.
     * @param section the table section of the spreadsheet to get.
     * @param vals the values represenatation of the spreadsheet.
     * @return said section as a map, indexed by Discord ID.
     *         null otherwise.
     */
    public TreeMap<Object, PlayerStats> readSection(
            String section, Values vals) {
        try {
            ValueRange response = vals.get(
                    getSpreadsheetID(), section).execute();
            List<List<Object>> values = response.getValues();

            TreeMap<Object, PlayerStats> table = new TreeMap<>();
            if (values != null && !values.isEmpty()) {
                for (int i = 1; i < values.size(); i++) {
                    List<Object> row = values.get(i);
                    Object id = row.remove(0);
                    PlayerStats rowStats = new PlayerStats(
                            Integer.toString(i + 1), row);

                    table.put(id, rowStats);
                }
                return table;
            }
        } catch (IOException e) {
            Events.ORIGIN.sendMessage(
                    "The data could not load.").queue();
        }

        return null;
    }

    /**
     * Appends a row to the end of the spreadsheet section.
     * @param section the table section of the spreadsheet to get.
     * @param vals the values represenatation of the spreadsheet.
     * @param row the row of values to append.
     * @source Twilio on YouTube.
     */
    public void appendRow(String section, Values vals, ValueRange row)
        throws IOException {
        vals.append(getSpreadsheetID(), section, row)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .setIncludeValuesInResponse(true).execute();
    }

    /**
     * Updates a row to the end of the spreadsheet section.
     * @param section the table section of the spreadsheet to get.
     * @param vals the values represenatation of the spreadsheet.
     * @param row the row of values to update to.
     * @source Twilio on YouTube.
     */
    public void updateRow(String section, Values vals, ValueRange row)
            throws IOException {
        vals.update(getSpreadsheetID(), section, row)
                .setValueInputOption("USER_ENTERED")
                .setIncludeValuesInResponse(true).execute();
    }
}

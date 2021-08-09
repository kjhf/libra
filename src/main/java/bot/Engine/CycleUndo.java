package bot.Engine;

import bot.Discord;
import bot.Tools.GoogleAPI;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values;

import java.io.File;
import java.util.List;
import java.util.Scanner;
import java.util.Collections;
import java.util.Arrays;
import java.util.TreeMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * @author  Wil Aquino
 * Date:    June 1, 2021
 * Project: LaunchPoint Bot
 * Module:  CycleUndo.java
 * Purpose: Reverts the Cycle spreadsheet to the previous state.
 */
public class CycleUndo extends CycleLog implements Command {

    /**
     * Retrieves the previous "lpcycle" or "lpsub" command.
     * @return said comand.
     */
    private String retrieveLastMessage() {
        File save = new File("load.txt");
        try {
            Scanner load = new Scanner(save);
            String message = load.nextLine();
            load.close();

            return message;
        } catch (FileNotFoundException ioe) {
            sendToDiscord("Something went wrong with the undo file.");
            ioe.printStackTrace();
            return null;
        }
    }

    /**
     * Updates a user's stats within a spreadsheet.
     * @param args the user input.
     * @param link a connection to the spreadsheet.
     * @param user the user to revert the stats of.
     * @param range the name of the spreadsheet section
     * @param sheetVals the values of the spreadsheet section.
     * @param table a map of all rows of the spreadsheet.
     * @param notSub a flag to check if the user is a sub or not.
     */
    private void undoUser(String[] args, GoogleAPI link, String user,
                          String range, Values sheetVals,
                          TreeMap<Object, PlayerStats> table, boolean notSub) {
        try {
            String userID = user.substring(3, user.length() - 1);
            PlayerStats player = table.get(userID);

            int cycleGamesPlayed = getGamesPlayed(args);
            int cycleGamesWon = getGamesWon(args);

            int setWins = player.getSetWins();
            int setLosses = player.getSetLosses();
            int gamesWon = player.getGamesWon();
            int gamesLost = player.getGamesLost();

            if (notSub) {
                if (cycleSetWon(cycleGamesWon, cycleGamesPlayed)) {
                    setWins--;
                } else {
                    setLosses--;
                }
            }
            gamesWon -= cycleGamesWon;
            gamesLost -= cycleGamesPlayed - cycleGamesWon;

            String updateRange = range + "!B" + player.getPosition()
                    + ":E" + player.getPosition();
            ValueRange newRow = new ValueRange().setValues(
                    Collections.singletonList(Arrays.asList(
                            player.getName(), player.getNickname(),
                            setWins, setLosses)));
            link.updateRow(updateRange, sheetVals, newRow);

            updateRange = range + "!H" + player.getPosition()
                    + ":I" + player.getPosition();
            newRow = new ValueRange().setValues(
                    Collections.singletonList(Arrays.asList(
                            gamesWon, gamesLost)));
            link.updateRow(updateRange, sheetVals, newRow);

            sendToDiscord(String.format(
                    "%s's leaderboard stats were reverted...",
                    player.getName()));
        } catch (IOException e) {
            sendToDiscord(String.format(
                    "User %s could not be reverted...",
                    user));
        }
    }

    /**
     * Checks if the user is a sub, then reverts a user's stats
     * within a spreadsheet.
     * @param args the user input.
     * @param link a connection to the spreadsheet.
     * @param user the user to revert the stats of.
     * @param range the name of the spreadsheet section
     * @param sheetVals the values of the spreadsheet section.
     * @param table a map of all rows of the spreadsheet.
     */
    private void undoUser(String[] args, GoogleAPI link, String user,
                          String range, Values sheetVals,
                          TreeMap<Object, PlayerStats> table) {
        undoUser(args, link, user, range, sheetVals, table, checkForSub(args));
    }

    /**
     * Runs the cycle undoing command.
     * @param outChannel the channel to output to, if it exists.
     * @param users the users to attach to the command output, if they exist.
     * @param args the arguments of the command, if they exist.
     */
    @Override
    public void runCmd(MessageChannel outChannel,
                       List<Member> users, String[] args) {
        try {
            GoogleAPI link = new GoogleAPI(Discord.getCyclesSheetID());

            // tab name of the spreadsheet
            String range = "'Current Leaderboard'";

            Values sheetVals = link.getSheet().spreadsheets().values();
            TreeMap<Object, PlayerStats> table = link.readSection(
                    range, sheetVals);
            if (table == null) {
                throw new IOException("The spreadsheet was empty.");
            }

            String lastMessage = retrieveLastMessage();
            if (lastMessage == null) {
                throw new IOException();
            } else if (lastMessage.equals("REDACTED")) {
                sendToDiscord("There is nothing to revert.");
                return;
            }

            String[] messageArgs = lastMessage.split("\\s+", 7);
            int userArgs = messageArgs.length - 3;

            for (int i = 1; i < 1 + userArgs; i++) {
                undoUser(messageArgs, link, messageArgs[i], range, sheetVals,
                        table);
            }

            sendToDiscord("The previous match report was reverted.");
        } catch (IOException | GeneralSecurityException e) {
            sendToDiscord("The save could not load.");
        }

        log("Cycle undo was processed.");
    }
}

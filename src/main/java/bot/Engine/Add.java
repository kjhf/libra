package bot.Engine;

import bot.Tools.Command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;

/**
 * @author  Wil Aquino
 * Date:    April 26, 2021
 * Project: Libra
 * Module:  Add.java
 * Purpose: Adds roles to users in LaunchPoint.
 */
public class Add extends Section implements Command {

    /**
     * Constructs the add attributes.
     * @param abbreviation the abbreviation of the section.
     */
    public Add(String abbreviation) {
        super(abbreviation);
    }

    /**
     * Allows a user entry into a section.
     * @param sc the user's inputted command.
     * @param playerID the Discord ID of the player to graduate.
     * @return the entrance welcome message.
     */
    private String enter(SlashCommandEvent sc, String playerID) {
        addRole(sc, playerID, getRole(sc, getSection()));
        String rulesChannelName = getPrefix() + "-draft-rules";

        String rulesChannel = getChannel(sc, rulesChannelName).getAsMention();
        return "Welcome to " + getSection() + "! Make sure to read "
                + rulesChannel + " before playing in any drafts!";
    }

    /**
     * Runs any role commands.
     * @param sc the inputted slash command.
     */
    @Override
    public void runCmd(SlashCommandEvent sc) {
        sc.deferReply().queue();
        List<OptionMapping> args = sc.getOptions();

        StringBuilder listOfUsers = new StringBuilder();
        for (OptionMapping om : args) {
            Member user = om.getAsMember();
            String welcomeMessage = enter(sc, user.getId());

            Member finalUser = args.get(args.size() - 1).getAsMember();
            if (user.equals(finalUser)) {
                listOfUsers.append(user.getAsMention())
                        .append("\n\n")
                        .append(welcomeMessage);
            } else {
                listOfUsers.append(user.getAsMention()).append(" ");
            }
        }

        sendResponse(sc, listOfUsers.toString(), false);
        log(args.size() + " new " + getSection()
                + " user(s) processed.", false);
    }
}

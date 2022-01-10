package bot.Engine.Drafts;

import bot.Engine.Section;
import bot.Tools.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author  Wil Aquino, Turtle#1504
 * Date:    December 6, 2021
 * Project: LaunchPoint Bot
 * Module:  StartDraft.java
 * Purpose: Starts a draft.
 */
public class StartDraft extends Section implements Command {

    /** Fields for the number of active drafts. */
    public static int numLPDrafts = 0;
    public static int numIODrafts = 0;

    /** The players of the draft. */
    private final List<Member> players;

    /** The pinged role for this draft. */
    private Role draftRole;

    /**
     * Constructs the initialized draft and initializes
     * the draft start attributes.
     * @param abbreviation the abbreviation of the section.
     * @param initialPlayer the first player of the draft.
     */
    public StartDraft(String abbreviation, Member initialPlayer) {
        super(abbreviation);
        players = new ArrayList<>();
        players.add(initialPlayer);
    }

    /**
     * Retrieves the players of the draft.
     * @return said players.
     */
    public List<Member> getPlayers() {
        return players;
    }

    /**
     * Sends a draft confirmation summary with all players
     * of the draft.
     * @param color the color of the summary.
     * @param channel the draft chat channel to attach to the report.
     */
    private void sendReport(Color color, TextChannel channel) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Draft Confirmation");
        eb.setColor(color);

        StringBuilder queue = new StringBuilder();
        StringBuilder logList = new StringBuilder();
        Random r = new Random();
        int capt1 = r.nextInt(8);
        int capt2 = r.nextInt(8);
        while (capt1 == capt2) {
            capt2 = r.nextInt(8);
        }

        for (int i = 0; i < players.size(); i++) {
            Member currPlayer = players.get(i);

            queue.append(currPlayer.getAsMention());
            logList.append(currPlayer.getEffectiveName()).append(" ");

            if (i == capt1 || i == capt2) {
                queue.append(" (captain)");
            }
            if (i < players.size()) {
                queue.append("\n");
            }
        }
        queue.append(players.get(players.size() - 1).getAsMention());

        eb.addField("Players", queue.toString(), false);
        eb.addField("Notice", "Go to " + channel.getAsMention() + "\n"
                + "to begin the draft.", false);

        logList.deleteCharAt(logList.length() - 1);
        log("A draft was started with " + logList + ".");
        sendEmbed(eb);
    }

    /**
     * Attempts to start a draft.
     * @param bc a button click to analyze.
     */
    public void attemptDraft(ButtonClickEvent bc) {
        // prevents repeats, uncomment after everything's working
//        if (players.contains(bc.getMember())) {
//            return;
//        }

        getPlayers().add(bc.getMember());
        String newCaption = draftRole.getAsMention() + " +" + (8 - getPlayers().size());
        bc.editMessage(newCaption).queue();

        if (getPlayers().size() == 8) {
            disableButton(bc, "Draft queue full.");

            String btnName = bc.getButton().getId();
            int numButton = Integer.parseInt(
                    btnName.substring(btnName.length() - 1));

            TextChannel draftChannel = getChannel(
                    getPrefix() + "-draft-chat-" + numButton);
            sendReport(getColor(), draftChannel);
        }
    }

    /**
     * Runs the draft start command.
     * @param cmd the formal name of the command.
     * @param args the arguments of the command, if they exist.
     */
    @Override
    public void runCmd(String cmd, List<OptionMapping> args) {
        String id;
        if (getPrefix().equals("lp")) {
            numLPDrafts++;
            id = "Join" + getPrefix().toUpperCase() + numLPDrafts;
        } else {
            numIODrafts++;
            id = "Join" + getPrefix().toUpperCase() + numIODrafts;
        }
        draftRole = getRole(getSection());

        String caption = draftRole.getAsMention() + " +7";
        String label = "Join Draft";

        sendButton(caption, id, label, 0);
        log("A " + getPrefix().toUpperCase() + " draft has been requested.");
    }
}
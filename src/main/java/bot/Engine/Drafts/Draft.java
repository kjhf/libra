package bot.Engine.Drafts;

import bot.Engine.Section;
import bot.Tools.Command;

import bot.Tools.Components;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.TreeMap;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author  Wil Aquino, Turtle#1504
 * Date:    December 6, 2021
 * Project: Libra
 * Module:  Draft.java
 * Purpose: Formalizes and starts a draft.
 */
public class Draft extends Section implements Command {

    /** Flag for checking whether this draft has initialized or not. */
    private boolean initialized;

    /** The time limit for the request to expire. */
    private int timeLimit;

    /** The starting time of this draft's initial request. */
    private final long startTime;

    /** The formal number of this draft. */
    private final int numDraft;

    /** The formal process for executing this draft. */
    private DraftProcess draftProcess;

    /** The players of this draft. */
    private final TreeMap<String, DraftPlayer> players;

    /** The amount of inactive players within the draft. */
    private int numInactive;

    /** The draft chat channel this draft is occurring in. */
    private final TextChannel draftChat;
//    private final TextChannel botTesting;

    /** The Discord message ID for this draft's initial interface. */
    private String messageID;

    /** The number of players required to formally start the draft. */
    public final static int NUM_PLAYERS_TO_START_DRAFT = 8;

    /** A random number generator. */
    private final Random numGenerator;

    /**
     * Constructs a draft template and initializes the
     * draft start attributes.
     * @param sc the user's inputted command.
     * @param draft the numbered draft that this draft is.
     * @param abbreviation the abbreviation of the section.
     * @param initialPlayer the first player of the draft.
     * @param generator a random number generator.
     */
    public Draft(SlashCommandEvent sc, int draft, String abbreviation,
                 Member initialPlayer, Random generator) {
        super(abbreviation);

        initialized = false;

        // time limit is 45 minutes initially
        timeLimit = 1000 * 60 * 45;
        startTime = System.currentTimeMillis();

        numDraft = draft;
        players = new TreeMap<>();
        numInactive = 0;

        DraftPlayer newPlayer = new DraftPlayer(
                initialPlayer.getEffectiveName(), false);
        players.put(initialPlayer.getId(), newPlayer);

        draftChat = getChannel(sc, getPrefix() + "-draft-chat-" + draft);
//        botTesting = getChannel(sc, "bot-testing");

        numGenerator = generator;
    }

    /**
     * Checks whether the draft request has been satisfied or not.
     * @return True if eight players have joined the draft.
     *         False otherwise.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Activates or deactivates the draft.
     * @param toStatus the initialization status to set the draft to.
     */
    public void toggleRequest(boolean toStatus) {
        initialized = toStatus;
    }

    /**
     * Retrieves which draft number this is.
     * @return said number.
     */
    public int getNumDraft() {
        return numDraft;
    }

    /**
     * Checks if the draft has formally started.
     * @return True if it has formally started.
     *         False otherwise.
     */
    private boolean draftStarted() {
        return getProcess() != null && getProcess().hasStarted();
    }

    /**
     * Checks whether a player can access a draft, according
     * to their draft section.
     * @param interaction the user interaction calling this method.
     * @return True if they are in the wrong section.
     *         False otherwise.
     */
    public boolean inWrongSection(GenericInteractionCreateEvent interaction) {
        return !interaction.getMember().getRoles().contains(
                getRole(interaction, getSection()));
    }

    /**
     * Retrieves the field for executing the draft.
     * @return said execution.
     */
    public DraftProcess getProcess() {
        return draftProcess;
    }

    /**
     * Retrieves the players of the draft.
     * @return said players.
     */
    public TreeMap<String, DraftPlayer> getPlayers() {
        return players;
    }

    /**
     * Retrieves the draft chat channel which this draft
     * is occurring in.
     * @return said channel.
     */
    public TextChannel getDraftChannel() {
        return draftChat;
    }

//    /**
//     * Retrieves the bot testing channel.
//     * @return said channel.
//     */
//    public TextChannel getTestingChannel() {
//        return botTesting;
//    }

    /**
     * Retrieves the request interface of the draft.
     * @param interaction the user interaction calling this method.
     * @return said message.
     */
    public Message getMessage(GenericInteractionCreateEvent interaction) {
        MessageChannel channel =
                getChannel(interaction, getPrefix() + "-looking-for-draft");
//                botTesting;
        return channel.retrieveMessageById(messageID).complete();
    }

    /**
     * Checks whether the draft request has timed out or not.
     * @param interaction the user interaction calling this method.
     * @return True if the request expired.
     *         False otherwise.
     */
    public boolean timedOut(GenericInteractionCreateEvent interaction) {
        long currentTime = System.currentTimeMillis();

        if (!isInitialized() && messageID != null
                && currentTime - startTime >= timeLimit) {
            getMessage(interaction)
                    .editMessage("This draft has expired.")
                    .setActionRow(Components.ForDraft.refresh(
                                    getPrefix() + getNumDraft())
                            .asDisabled()).queue();

            String update = "A " + getPrefix().toUpperCase()
                    + " draft request has timed out.";
            log(update, false);
            return true;
        }

        return false;
    }

    /**
     * Sends a draft confirmation summary with all players
     * of the draft.
     * @param interaction the user interaction calling this method.
     */
    private void updateReport(GenericInteractionCreateEvent interaction) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Draft Queue " + getNumDraft());
        eb.setColor(getColor());

        StringBuilder players = new StringBuilder();
        StringBuilder subs = new StringBuilder();
        StringBuilder logList = new StringBuilder();

        for (Map.Entry<String, DraftPlayer> mapping : getPlayers().entrySet()) {
            String id = mapping.getKey();
            DraftPlayer player = mapping.getValue();
            boolean isCaptain = player.isCaptainForTeam1()
                    || player.isCaptainForTeam2();

            if (!player.isActive()) {
                subs.append(player.getAsMention(id))
                        .append(" (inactive)").append("\n");
            } else if (player.isSub()) {
                subs.append(player.getAsMention(id)).append("\n");
            } else if (isCaptain) {
                players.append(player.getAsMention(id))
                        .append(" (captain)").append("\n");
            } else {
                players.append(player.getAsMention(id)).append("\n");
            }

            logList.append(player.getName()).append(", ");
        }

        eb.addField("Players:", players.toString(), false);
        if (subs.length() > 0) {
            eb.addField("Subs:", subs.toString(), false);
        }

        if (getPlayers().size() >= NUM_PLAYERS_TO_START_DRAFT) {
            String notice =
                    "Go to (the pinged) "
                            + getDraftChannel().getAsMention() + "\n"
                            + "to begin the draft. Use this interface\n"
                            + "to sub out as needed. __Before you\n"
                            + "sub out -- make sure you got your\n"
                            + "points in the draft chat!__";
            eb.addField("Notice:", notice, false);

            logList.deleteCharAt(logList.length() - 1);
            if (!isInitialized()) {
                log("A " + getPrefix().toUpperCase() + " draft was started with "
                        + logList + ".", false);
            }
        } else {
            long approxTime = (startTime + timeLimit) / 1000;
            eb.addField("Expiration:", "<t:" + approxTime + ":R>", false);
        }

        sendEmbed(interaction, eb);
    }

    /**
     * Formats a ping for gathering players.
     * @return the request.
     */
    private String newPing() {
        int activePlayers = players.size() - numInactive;
        int pingsLeft = NUM_PLAYERS_TO_START_DRAFT - activePlayers;
        if (draftStarted()) {
            pingsLeft = 0;
        }

        return getSectionRole() + " +" + pingsLeft;
    }

    /**
     * Determines the captains of the draft.
     * @param oldCaptainID the Discord ID of a previous captain.
     * @return the captains of the draft.
     */
    public TreeMap<Integer, String> determineCaptains(String oldCaptainID) {
        List<String> ids = new ArrayList<>(getPlayers().keySet());
        if (oldCaptainID != null) {
            ids.remove(oldCaptainID);
        }

        TreeMap<Integer, String> captainIDs = new TreeMap<>();
        int numCaptains = 0;

        if (isInitialized()) {
            for (Map.Entry<String, DraftPlayer> mapping : getPlayers().entrySet()) {
                String playerID = mapping.getKey();
                DraftPlayer player = mapping.getValue();

                if (player.isCaptainForTeam1()) {
                    captainIDs.put(1, playerID);
                    numCaptains++;
                } else if (player.isCaptainForTeam2()) {
                    captainIDs.put(2, playerID);
                    numCaptains++;
                }
            }
        }

        while (numCaptains < 2) {
            int size = ids.size();
            String randomID = ids.get(numGenerator.nextInt(size));
            DraftPlayer randomPlayer = getPlayers().get(randomID);
            boolean isCaptain = randomPlayer.isCaptainForTeam1()
                    || randomPlayer.isCaptainForTeam2();

            if (!randomPlayer.isSub() && !isCaptain) {
                if (captainIDs.get(1) == null) {
                    randomPlayer.setCaptainForTeam1(true);
                    captainIDs.put(1, randomID);
                } else if (captainIDs.get(2) == null) {
                    randomPlayer.setCaptainForTeam2(true);
                    captainIDs.put(2, randomID);
                }

                numCaptains++;
            }
        }

        return captainIDs;
    }

    /**
     * Attempts to start a draft.
     * @param bc a button click to analyze.
     */
    public void attemptDraft(ButtonClickEvent bc) {
        String playerID = bc.getMember().getId();

        if (inWrongSection(bc)) {
            sendReply(bc, "You don't have access to this section's drafts!", true);
            return;
        } else if (getPlayers().containsKey(playerID)) {
            sendReply(bc, "You are already in this draft!", true);
            return;
        } else if (getPlayers().size() >= (NUM_PLAYERS_TO_START_DRAFT / 2) - 1) {
            // add 5 minutes
            timeLimit += 1000 * 60 * 5;
        }

        DraftPlayer newPlayer = new DraftPlayer(
                bc.getMember().getEffectiveName(), false);
        getPlayers().put(playerID, newPlayer);

        if (getPlayers().size() == NUM_PLAYERS_TO_START_DRAFT) {
            draftProcess = new DraftProcess(this);
            List<Button> buttons = new ArrayList<>();

            String idSuffix = getPrefix().toUpperCase() + getNumDraft();

            buttons.add(Components.ForDraft.reassignCaptain(idSuffix));
            buttons.add(Components.ForDraft.refresh(idSuffix));
            buttons.add(Components.ForDraft.requestSub(idSuffix));
            buttons.add(Components.ForDraft.joinAsSub(idSuffix));

            sendButtons(bc, bc.getInteraction().getMessage().getContentRaw(),
                    buttons);
            determineCaptains(null);
            toggleRequest(true);
            refresh(bc);

            getProcess().refresh(null);
        } else {
            refresh(bc);
            messageID = bc.getMessageId();
        }
    }

    /**
     * Reassigns a captain of the draft, via a button.
     * @param bc a button click to analyze.
     */
    public void reassignCaptain(ButtonClickEvent bc) {
        String authorID = bc.getMember().getId();
        DraftPlayer author = getPlayers().get(authorID);

        if (author == null) {
            sendReply(bc, "You are not in this draft!", true);
            return;
        } else if (!author.isCaptainForTeam1() && !author.isCaptainForTeam2()) {
            sendReply(bc, "Only captains can reassign themselves.", true);
            return;
        } else if (author.isCaptainForTeam1()){
            author.setCaptainForTeam1(false);
            getProcess().getTeam1().clear();
        } else {
            author.setCaptainForTeam2(false);
            getProcess().getTeam2().clear();
        }

        determineCaptains(authorID);
        updateReport(bc);
        refresh(bc);
    }

    /**
     * Refreshes the draft request's caption.
     * @param bc a button click to analyze.
     */
    public void refresh(ButtonClickEvent bc) {
        bc.deferEdit().queue();
        messageID = bc.getMessageId();

        int activePlayers = getPlayers().size() - numInactive;

        if (activePlayers == NUM_PLAYERS_TO_START_DRAFT || !draftStarted()) {
            editMessage(bc, newPing());
        } else {
            int subsNeeded = NUM_PLAYERS_TO_START_DRAFT - activePlayers;
            editMessage(bc,
                    newPing() + "   // " + subsNeeded + " sub(s) needed");
        }

        updateReport(bc);
    }

    /**
     * Repings for remaining players.
     * @param bc a button click to analyze.
     */
    public void reping(ButtonClickEvent bc) {
        String authorID = bc.getMember().getId();
        messageID = bc.getMessageId();

        // set timer to 15 minutes
        long currentTime = System.currentTimeMillis();
        long waitTime = 1000 * 60 * 15;

        if (!getPlayers().containsKey(authorID)) {
            sendReply(bc, "You're not in this draft!", true);
        } else if (currentTime - startTime < waitTime) {
            long approxTime = (startTime + waitTime) / 1000;
            sendReply(bc,
                    String.format("Wait until <t:%s:t> to reping!", approxTime),
                    true);
        } else {
            bc.deferEdit().queue();

            String idSuffix = getPrefix().toUpperCase() + getNumDraft();
            bc.editButton(
                    Components.ForDraft.reping(idSuffix).asDisabled()).queue();
            sendResponse(bc, newPing() + " (reping)", false);
        }
    }

    /**
     * Removes a player from the draft, if possible, via a button.
     * @param bc a button click to analyze.
     */
    public void removeFromQueue(ButtonClickEvent bc) {
        String playerID = bc.getMember().getId();
        messageID = bc.getMessageId();

        if (!getPlayers().containsKey(playerID)) {
            sendReply(bc, "You are not in this draft!", true);
        } else {
            getPlayers().remove(playerID);
            refresh(bc);
        }
    }

    /**
     * Checks whether a player is in Team 1 or not.
     * @param playerID the Discord ID of the player to check.
     * @return True if they are in Team 1.
     *         False otherwise.
     */
    private boolean teamOneContains(String playerID) {
        return getProcess() != null
                && getProcess().getTeam1().contains(playerID);
    }

    /**
     * Checks whether a player is in Team 2 or not.
     * @param playerID the Discord ID of the player to check.
     * @return True if they are in Team 2.
     *         False otherwise.
     */
    private boolean teamTwoContains(String playerID) {
        return getProcess() != null
                && getProcess().getTeam2().contains(playerID);
    }

    /**
     * Attempts to sub out a player from the draft.
     * @param interaction the user interaction calling this method.
     * @param playerID the Discord ID of the player.
     * @param player the player to sub out.
     * @param notFoundString a string to output if the player could not be found.
     * @param subbedTwiceString a string to output if the player has already
     *                          been subbed out from the draft.
     * @return True if the substitution was successful.
     *         False otherwise.
     */
    private boolean subOut(GenericInteractionCreateEvent interaction,
                        String playerID, DraftPlayer player,
                        String notFoundString, String subbedTwiceString) {
        if (player == null) {
            sendResponse(interaction, notFoundString, true);
            return false;
        } else if (!player.isActive()) {
            sendResponse(interaction, subbedTwiceString, true);
            return false;
        } else if (teamOneContains(playerID)) {
            getProcess().getTeam1().requestSub();
        } else if (teamTwoContains(playerID)) {
            getProcess().getTeam2().requestSub();
        }

        player.setSubStatus(true);
        player.setActiveStatus(false);

        if (!draftStarted()) {
            player.setCaptainForTeam1(false);
            player.setCaptainForTeam2(false);
            determineCaptains(playerID);
        }

        return true;
    }

    /**
     * Requests a sub for a draft, if possible, via a button.
     * @param bc a button click to analyze.
     */
    public void requestSub(ButtonClickEvent bc) {
        String playerID = bc.getMember().getId();
        DraftPlayer foundPlayer = getPlayers().get(playerID);

        boolean subSuccessful = subOut(bc, playerID, foundPlayer,
                "You are not in this draft!",
                "You have already been subbed out of the draft!");

        if (subSuccessful) {
            String update = getSectionRole() + " sub requested for Queue "
                    + getNumDraft();
            sendResponse(bc, update, false);

            numInactive++;
        }

        refresh(bc);
    }

    /**
     * Forcibly subs a person out of the draft.
     * @param sc a slash command to analyze.
     * @param playerID the Discord ID of the player to sub.
     */
    public void forceSub(SlashCommandEvent sc, String playerID) {
        String authorID = sc.getMember().getId();
        if (!getPlayers().containsKey(authorID)) {
            sendReply(sc, "You don't have access to that draft!", true);
        } else if (!isInitialized()) {
            sendReply(sc, "There's no point in subbing people out currently!", true);
        } else if (subOut(sc, playerID, getPlayers().get(playerID),
                "That player is not part of the draft.",
                "That player has already been subbed out of the draft!")) {
            String update = getSectionRole() + " sub requested for Queue "
                    + getNumDraft() + " (see above draft after refreshing)";
            sendReply(sc, update, false);

            numInactive++;
        }
    }

    /**
     * Subs a player into the draft.
     * @param playerID the Discord ID of the player.
     * @param name the name of the player.
     * @return an update string based on their addition.
     */
    private String subIn(String playerID, String name) {
        DraftPlayer player;
        String statement;

        if (getPlayers().containsKey(playerID)) {
            player = getPlayers().get(playerID);
            player.setActiveStatus(true);
            player.setSubStatus(draftStarted());

            if (teamOneContains(playerID)) {
                getProcess().getTeam1().add(playerID, player);
            } else if (teamTwoContains(playerID)) {
                getProcess().getTeam2().add(playerID, player);
            }

            statement = "will be coming back to sub";
        } else {
            player = new DraftPlayer(name, draftStarted());
            getPlayers().put(playerID, player);

            statement = "will be subbing";
        }

        return player.getAsMention(playerID) + " "
                + statement + " for this draft in "
                + getDraftChannel().getAsMention() + ".";
    }

    /**
     * Adds a player to the draft's subs, if possible, via a button.
     * @param bc a button click to analyze.
     */
    public void addSub(ButtonClickEvent bc) {
        Member player = bc.getMember();
        int activePlayers = players.size() - numInactive;

        if (inWrongSection(bc)) {
            sendReply(bc, "You don't have access to this section's drafts!", true);
        } else if (activePlayers == NUM_PLAYERS_TO_START_DRAFT) {
            sendReply(bc, "This draft hasn't requested any subs yet.", true);
        } else {
            String update = subIn(player.getId(), player.getEffectiveName());
            refresh(bc);
            sendResponse(bc, update, false);
        }
    }

    /**
     * Forcibly ends the draft.
     * @param sc a slash command to analyze.
     * @return True if the draft was forcibly ended.
     *         False otherwise.
     */
    public boolean forceEnd(SlashCommandEvent sc) {
        if (messageID == null) {
            sendReply(sc, "Press the `Refresh` button twice then "
                    + "try again.", true);
            return false;
        } else {
            getChannel(sc, getPrefix() + "-looking-for-draft")
                    .retrieveMessageById(messageID).complete()
                    .editMessage("This draft has forcibly ended.")
                    .setActionRow(Components.ForDraft.refresh(
                                    getPrefix() + getNumDraft())
                            .asDisabled()).queue();

            if (getProcess() != null && getProcess().getMessageID() != null) {
                getProcess().getMessage().delete().queue();
                sendResponse(sc, "Draft ended.", false);
            } else {
                String update = "Draft ended. **Check** "
                        + getDraftChannel().getAsMention() + " **to delete the "
                        + "teams interface. It may be pinned.**";
                sendResponse(sc, update, false);
            }

            return true;
        }
    }

    /**
     * Runs the draft start command.
     * @param sc the user's inputted command.
     */
    @Override
    public void runCmd(SlashCommandEvent sc) {
        if (inWrongSection(sc)) {
            sendReply(sc, "You don't have access to this section's drafts!", true);
            return;
        }

        ArrayList<Button> buttons = new ArrayList<>();
        String idSuffix = getPrefix().toUpperCase() + getNumDraft();
        buttons.add(Components.ForDraft.joinDraft(idSuffix));
        buttons.add(Components.ForDraft.refresh(idSuffix));
        buttons.add(Components.ForDraft.reping(idSuffix));
        buttons.add(Components.ForDraft.leave(idSuffix));

        String caption = getSectionRole() + " +" + (NUM_PLAYERS_TO_START_DRAFT - 1);
        sc.reply(caption).addActionRow(buttons).queue();

        log("A " + getPrefix().toUpperCase()
                + " draft has been requested.", false);
    }
}

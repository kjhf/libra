package bot.Engine.Drafts;

import java.util.TreeMap;

/**
 * @author  Wil Aquino
 * Date:    April 1, 2022
 * Project: Libra
 * Module:  DraftTeam.java
 * Purpose: Represents a team within a draft.
 */
public class DraftTeam {

    /** The players of the team. */
    private final TreeMap<String, DraftPlayer> players;

    /** The team's total score. */
    private int score;

    /** The amount of players a team needs. */
    private int playersNeeded;

    /** Starts a team within a draft. */
    public DraftTeam() {
        players = new TreeMap<>();
        score = 0;
        clear();
    }

    /**
     * Retrieves the players of the team.
     * @return said players.
     */
    public TreeMap<String, DraftPlayer> getPlayers() {
        return players;
    }

    /**
     * Retrieves the team's total score.
     * @return said score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Adds a win to the team's score.
     */
    public void incrementScore() {
        score++;
    }

    /**
     * Subtracts a win from the team's score.
     */
    public void decrementScore() {
        score--;
    }

    /**
     * Checks whether the team needs a sub or not.
     * @return True if a sub is needed.
     *         False otherwise.
     */
    public boolean needsPlayers() {
        return playersNeeded > 0;
    }

    /**
     * Substitutes a player out of the active players of the team.
     * @param id the Discord ID of the player.
     */
    public void subOut(String id) {
        DraftPlayer player = getPlayers().get(id);

        player.makeSub();
        player.setInactive();

        playersNeeded++;
    }

    /**
     * Adds a player to the team.
     * @param id the Discord ID of the player.
     * @param player the player to add.
     */
    public void add(String id, DraftPlayer player) {
        if (!contains(id)) {
            getPlayers().put(id, player);
            player.setTeamStatus(true);
            playersNeeded--;
        }
    }

    /** Clears the team of its players. */
    public void clear() {
        for (DraftPlayer player : getPlayers().values()) {
            player.setTeamStatus(false);
        }
        getPlayers().clear();
        playersNeeded = Draft.NUM_PLAYERS_TO_START_DRAFT / 2;
    }

    /**
     * Checks whether a player is within the team already.
     * @param id the Discord ID of the player to check.
     * @return True if they are in the team.
     *         False otherwise.
     */
    public boolean contains(String id) {
        return getPlayers().containsKey(id);
    }
}

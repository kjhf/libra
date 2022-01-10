package bot.Engine;

import bot.Discord;

import java.awt.Color;

/**
 * @author  Wil Aquino
 * Date:    January 10, 2022
 * Project: LaunchPoint Bot
 * Module:  Section.java
 * Purpose: Designates MIT section specific information.
 */
public class Section {

    /** The name of the MIT section. */
    private final String name;

    /** The abbreviation of the section. */
    private final String prefix;

    /** The color of the section. */
    private final Color color;

    /** Graduates Google Sheets ID. */
    private final String gradSheetID;

    /** Public Cycles Google Sheets ID. */
    private final String cyclesSheetID;

    /**
     * Constructs the section attributes.
     * @param abbreviation the abbreviation of the section.
     */
    public Section(String abbreviation) {
        prefix = abbreviation;

        if (prefix.equals("lp")) {
            name = "LaunchPoint";
            color = Color.GREEN;
            gradSheetID = Discord.getLPGradSheetID();
            cyclesSheetID = Discord.getLPCyclesSheetID();
        } else {
            name = "Ink Odyssey";
            color = Color.MAGENTA;
            gradSheetID = Discord.getIOGradSheetID();
            cyclesSheetID = Discord.getIOCyclesSheetID();
        }
    }

    /**
     * Retrieves the MIT section name.
     * @return said section.
     */
    public String getSection() {
        return name;
    }

    /**
     * Retrieves the prefix of the MIT section.
     * @return said prefix.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Retrieves the designated color of the MIT section.
     * @return said color.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Retrieves the section's graduates sheet ID.
     * @return said ID.
     */
    public String gradSheetID() {
        return gradSheetID;
    }

    /**
     * Retrieves the section's cycles sheet ID.
     * @return said ID.
     */
    public String cyclesSheetID() {
        return cyclesSheetID;
    }
}
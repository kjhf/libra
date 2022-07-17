# Libra Design Documentation

![Libra](img/mit_libra.png)
**Author(s):** Wil Aquino

**Honorable Mention(s):** Turtle#1504

**Creation Date:** February 17, 2021

**Last Updated:** July 15, 2022

**Formal Documentation:** <medium><a href='https://docs.google.com/document/d/1LoYjd2mqadu5g5D-BMNHfLk9zUouZZPzLWriu-vxCew/edit?usp=sharing'>How To Use Libra - MullowayIT's Bot Documentation</a></medium>

**Table of Contents:**
* [Introduction](#introduction)
* [How to Install](#how-to-install)
* [Classes and Data Structures](#classes-and-data-structures)
  - [Main](#main)
  - [Config](#config)
  - [Events](#events)
  - [Command](#command)
  + [Tools](#tools)
    - [ArrayHeapMinPQ](#arrayheapminpq)
    - [ButtonBuilder](#buttonbuilder)
    - [Command](#command)
    - [Components](#components)
    - [DiscordWatch](#discordwatch)
    - [FileHandler](#filehandler)
    - [GoogleSheetsAPI](#googlesheetsapi)
    - [SelectionMenuBuilder](#selectionmenubuilder)
  + [Engine](#engine)
    - [Add](#add)
    - [Award](#award)
    - [Graduate](#graduate)
    - [PlayerStats](#playerstats)
  + [Cycles (Engine)](#cycles-engine)
    - [AutoLog](#autolog)
    - [ManualLog](#manuallog)
    - [PointsCalculator](#pointscalculator)
  + [Drafts (Engine)](#drafts-engine)
    - [Draft](#draft)
    - [DraftPlayer](#draftplayer)
    - [DraftProcess](#draftprocess)
    - [DraftTeam](#draftteam)
    - [MapGenerator](#mapgenerator)
    - [Undo](#undo)
  + [Profiles (Engine)](#profiles-engine)
    - [PlayerInfo](#playerinfo)
    - [Profile](#profile)
* [Persistence](#persistence)
* [Licensing and Rights](#licensing-and-rights)



## Introduction
Libra is a multi-functional Discord bot designed to handle many tasks, with respect to the Mulloway Institute of Turfing (MIT), a draft server for Nintendo's competitive shooter IP, Splatoon. Her main goal is to keep track of a player database, implement convenience features for running tournaments, and most importantly, host an automated draft system from beginning (queuing players) to end (reporting scores).



## How to Install
After pulling this project, you can import it using the <medium><a href='https://www.jetbrains.com/help/idea/gradle.html'>JetBrains' official IntelliJ Gradle documentation</a></medium>. All dependencies will be imported upon starting up the project with Gradle (often included with IntelliJ). If another IDE or medium is being used, Gradle must be installed to import the project dependencies.

To fully configure the bot, the credentials listed in `ConfigExample.java` must be filled out in its entirety. Afterwards, the file must be renamed to `Config.java` for integration.

The main module to run is `Main.java` but to start the bot from the console, run the following:
```
javac *
java src/main/java/bot/Main.java
```

----



## Classes and Data Structures
#### Main

The entry point of the bot. It constructs the bot and prepares it for processing commands.

###### Instance Variables
1. `String NAME` - the name of the bot.
2. `Color mitColor` - the color of MIT.
3. `Color freshwatershoalsColor` - the color of Freshwater Shoals.
4. `Color launchpointColor` - the color of LaunchPoint.
5. `Color inkodysseyColor` - the color of Ink Odyssey.
6. `Color inkodysseygraduateColor` - the color of Ink Odyssey graduates.

----

#### Config

A class consisting of credential-specific information, pertaining to Discord and the bot's persistence. Due to the credentials of this class, the secrets have been redacted. See `ConfigExample.java` for a template.

----

#### Events

The class which parses through user-inputted commands, as referenced in `Usage`.

###### Instance Variables
1. `Random RANDOM_GENERATOR` - a random number generator for the bot. 
2. `int MAX_FS_DRAFTS` - the maximum number of Freshwater Shoals drafts.
3. `int MAX_LP_DRAFTS` - the maximum number of LaunchPoint drafts.
4. `int MAX_IO_DRAFTS` - the maximum number of Ink Odyssey drafts.
5. `TreeMap<Integer, Draft> fsDrafts` - a map of numbers/buttons to Freshwater Shoals drafts.
6. `TreeMap<Integer, Draft> lpDrafts` - a map of numbers/buttons to LaunchPoint drafts.
7. `TreeMap<Integer, Draft> ioDrafts` - a map of numbers/buttons to Ink Odyssey drafts.
8. `ArrayHeapMinPQ<Integer> fsQueue` - a queue of numbered Freshwater Shoals drafts.
9. `ArrayHeapMinPQ<Integer> lpQueue` - a queue of numbered LaunchPoint drafts.
10. `ArrayHeapMinPQ<Integer> lpQueue` - a queue of numbered Ink Odyssey drafts.

----

### Tools

#### ArrayHeapMinPQ

A class which builds a minimum heap priority queue (This class is taken from another project and is therefore not detailed here. See the class itself for more details).

----

#### ButtonBuilder

A class which builds a button quickly.

###### Instance Variables
1. `Button button` - the button that was built.

----

#### Command

An interface outlining the format of the bot's command implementations.

----

#### Components

A class for storing components used throughout the bot.

----

## DiscordWatch

A class used as a timer and clock for Discord.

###### Instance Variables
1. `long timerStart` - the starting time of the first timer of the watch.
2. `long timerDuration` - how long the first timer of the watch should last.
3. `long timerStart2` - the starting time of the second timer of the watch.
4. `long timerDuration2` - how long the second timer of the watch should last.

----

#### FileHandler

A class which handles files (currently only for saving text).

###### Instance Variables
1. `File file` - an object representation for a text file.

----

#### GoogleSheetsAPI

A class which navigates a Google Sheet (spreadsheet).

###### Instance Variables
1. `Sheets sheetsService` - an object representation for the Google Sheets SDK.
2. `String spreadsheetID` - the credential ID of the spreadsheet.

----

#### SelectionMenuBuilder

A class which builds a selection menu quickly.

###### Instance Variables
1. `SelectionMenu menu` - the menu that was built.

----

### Engine

#### Add

A class which enters players into MIT, processing the command `lp/io add`.

----

#### Award

A class which awards players roles within MIT, processing the command `lp/io award`.

----

#### Graduate

A class which graduates a user in a section within MIT, processing the command `lp/io grad`, granting the associated roles.

###### Instance Variables
1. `String TAB` - the tab to reference within the profiles spreadsheet.

----

#### PlayerStats

A class for storing information about a player within MIT.

###### Instance Variables
1. `int numRow` - the numbered row of the player's data within their associated draft spreadsheet.
2. `String tag` - the player's Discord tag.
3. `String nickname` - the player's nickname on the server.
4. `String friendcode` - the player's Nintendo Switch friend code.
5. `String playstyle` - the player's preferred playstyle in-game.
6. `String weapons` - the player's preferred weapons in-game.
7. `String rank` - the player's average rank in-game.
8. `String team` - the player's competitive team, if any.

----

#### Section

A class for parenting MIT section-specific commands.

###### Instance Variables
1. `String name` - the name of this section.
2. `String prefix` - the prefix of this section. 
3. `String role` - the role of this section.
4. `String emote` - the emote of this section.
5. `Color color` - the color of this section.
6. `String gradSheetID` - the graduates spreadsheet ID for this section.
7. `String cyclesSheetID` - the Cycles spreadsheet ID for this section.
8. `String calculationsSheetID` - the calculations spreadsheet ID for this section.
9. `String CYCLES_TAB` - the tab to reference within the Cycles spreadsheet.
10. `String CYCLES_START_COLUMN` - the Cycles spreadsheet column that starts the needed information.
11. `String CYCLES_END_COLUMN` - the Cycles spreadsheet column that ends the needed information.

----

### Cycles (Engine)

#### AutoLog

A class which automatically updates the draft stats of a user.

----

#### ManualLog

A class which manually updates the draft stats of a user by processing the `lp/io log` and `lp/io sub` commands.

----

#### PointsCalculator

A class which calculates MIT leaderboard points for cycle changes.

###### Instance Variables
1. `int MAX_CATGEORY_POINTS` - the maximum amount of points per scoring category.
2. `int NUM_TOTAL_SCORES` - the total scoring categories to calculate.
3. `char SCORE_COLUMNS_START` - the first column where points are inputted.

----

### Drafts (Engine)

#### Draft

A class which forms and starts drafts, processing the command `lp/io startdraft` and handling other commands such as `lp/io forcesub`, etc.

###### Instance Variables
1. `boolean initialized` - a flag for checking if a draft has been initialized.
2. `DiscordWatch watch` - a watch to use throughout the draft.
3. `int numDraft` - the formal number of the draft, with respect to the draft maps in `Events`.
4. `DraftProcess draftProcess` - the formal process for the draft's execution.
5. `TreeMap<String, DraftPlayer> players` - the players of the draft.
6. `HashSet<String> playerHistory` - a history of players which have entered the draft queue at any point.
7. `int numInactive` - the number of inactive players within the draft.
8. `TextChannel draftChat` - the draft chat which this draft is linked to.
9. `String messageID` - the Discord message ID of the draft request.
10. `int NUM_PLAYERS_TO_START_DRAFT` - the number of players to start the draft.
11. `int mapGens` - the amount of times a map generation has occurred for the draft.

----

#### DraftPlayer

A class which represents a player within a draft.

###### Instance Variables
1. `String name` -  the name of the player.
2. `boolean active` - a flag for checking whether the player is active in the draft or not.
3. `boolean captainStatus1, captainStatus2` - flags for checking captaincy with respect to the two teams of the draft.
4. `boolean teamStatus` - a flag for checking the player's team status.
5. `boolean subStatus` - a flag for checking the player's sub status.
6. `int subs` - the number of times the player subbed out.
7. `int MINIMUM_POINTS` - the minimum amount of points a player can have.
8. `int MAXIMUM_POINTS` - the maximum amount of points a player can have.
9. `int matchWins` - the player's match wins during the draft.
10. `int matchLosses` - the player's match losses during the draft.

----

#### DraftProcess

A class which manages and processes drafts.

###### Instance Variables
1. `boolean started` - a flag for checking if the draft has started.
2. `Draft draft` - the draft which is to be processed.
3. `DraftTeam team1, team2` - the teams of the draft.
4. `int MAX_SCORE` - the maximum score for a team.
5. `int NUM_PLAUERS_TO_END_DRAFT_BEFORE_START` - the number of players required to formally end the draft, if it hasn't actually started yet.
6. `int NUM_PLAYERS_TO_END_DRAFT_AFTER_START` - the number of players required to formally end the draft, if it has started already.
7. `HashSet<String> endButtonClicked` - the players who have clicked the "End Draft" button consecutively.
8. `String messageID` - the Discord message ID of the draft request.

----

#### DraftTeam

A class which represents teams within a draft.

###### Instance Variables
1. `TreeMap<String, DraftPlayer> players` - the players of the team.
2. `int score` - the team's total score.
3. `int MIN_SCORE` - a team's minimum amount of points to gain.
4. `int MAX_SCORE` - a team's maximum amount of points to gain.
5. `int playersNeeded` - the amount of active players a team needs, at any given time.

----

#### MapGenerator

A class which generates map lists.

###### Instance Variables
1. `Draft foundDraft` - a draft associated with the map generator.
2. `int MAX_DRAFT_MAPLISTS` - the maximum number of map generations for a draft.

----

#### Undo

A class which reverts draft commands, processing the command `lp/io undo`.

----

### Profiles (Engine)

#### PlayerInfo

A class for storing information about a player within MIT.

###### Instance Variables
1. `int numRow` - the numbered row of the player's data within their associated draft spreadsheet.
2. `String tag` - the player's Discord tag.
3. `String nickname` - the player's nickname on the server.
4. `String friendcode` - the player's Nintendo Switch friend code.
5. `String pronouns` - the player's preferred pronouns.
6. `String playstyle` - the player's preferred playstyle.
7. `String weapons` - the player's preferred main weapons.
8. `String rank` - the player's average rank in-game.
9. `String team` - the player's competitive team, if any.

----

#### Profile

A class which manages the profile database of MIT.

###### Instance Variables
1. `String spreadsheetID` - the profiles spreadsheet ID.
2. `String START_COLUMN` - the starting information column of the profiles spreadsheet.
3. `String END_COLUMN` - the ending information column of the profiles spreadsheet.
4. `String TAB` - the tab to reference within the profiles spreadsheet.



## Persistence

The project saves and loads data from four Google Sheets spreadsheets, two each for the `Log` and `Graduate` classes.

These spreadsheets are connected and interacted with using the Google Sheets API, linked through the Gradle components of this project. Feature summary updates are also sent, through the channel the user originally typed commands in, by the bot using the Discord JDA API, also linked through Gradle.

The `lp/io undo` commands allows a user to revert a cycle command, by saving and loading the previous cycle command, saved in text files.

The `badwords.txt` text file, partially created by `nantonakos`, is referenced when finding profanity within phrases.

----




## Licensing and Rights

For more info on licensing and copyright, see the information listed under licensing file `LICENSE`.

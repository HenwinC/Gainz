package com.games.gobigorgohome.app;


import com.games.gobigorgohome.*;
import com.games.gobigorgohome.characters.Player;
import com.games.gobigorgohome.parsers.ParseJSON;
import com.games.gobigorgohome.parsers.ParseTxt;
import org.w3c.dom.ls.LSOutput;


import javax.crypto.spec.PSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.games.gobigorgohome.Colors.*;


public class Game {

    public static ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes());
    private GUI gui = GUI.getInstance();
    private InputOutput prompter = new InputOutput(gui);
    boolean isGameOver = false;
    private final Gym gym = Gym.getInstance();
    private final Player player = new Player();
    private final int energy = player.getEnergy();
    private final int currentEnergy = player.getEnergy();
    private final String playerName = player.getName();
    private String currentRoomName = gym.getStarterRoomName();
    private Room currentRoom = gym.getStarterRoom();
    private final Object rooms = gym.getRooms();
    private boolean fightOver = false;

    private final ParseTxt page = new ParseTxt();
    private final ParseJSON jsonParser = new ParseJSON();

//    public Game(InputOutput prompter) throws IOException, ParseException {
//        this.prompter = prompter;
//    }

    //    collects current input from user to update their avatar
    private void getNewPlayerInfo() {
//        TODO: validate user input
        String playerName = validName();
        double playerHeight = validDouble("What is your height? ", "height", "inches");
        double playerWeight = validDouble("What is your weight? ", "weight", "lbs");
        int playerAge = validInt("What is your age", "age", "years");
        createPlayer(playerName, playerAge, playerHeight, playerWeight);
    }

    // validates name requesting one and rejecting empty space(s).
    private String validName() {
        String playerName = prompter.prompt("What is your name? ");
        if (playerName.isBlank() || playerName.isEmpty() || playerName.length() > 16) {
            try {
                prompter.info("You need to type your name or it exceeds 16 characters: ");
                validName();
            } catch (NullPointerException e) {
                prompter.info("You need to type your name: ");
                validName();
            }
        } else {
            prompter.info("Hello " + playerName + " let's get more about you...");
        }
        return playerName;
    }

    // validates height and weight taking integers or doubles only
    private double validDouble(String msg, String measureName, String unit) {
        String measurementString = prompter.prompt(msg);
        double measurement = 0;
        try {
            measurement = Double.parseDouble(measurementString);
            //validDouble(measure, "you need to type your " + measureName + " in " + unit + ": ", measureName, unit);
        } catch (NumberFormatException | NullPointerException e) {
            validDouble("You need to type your " + measureName + " using numbers (" + unit + "): ", measureName, unit);
        }
        return measurement;
    }

    // validates age taking only an integer
    private int validInt(String msg, String measureName, String unit) {
        String measurement = prompter.prompt(msg);
        int measureNum = 0;
        try {
            measureNum = Integer.parseInt(measurement);
            //validInt(measure, "you need to type your "+ measureName+" in " + unit + " or you aren't an adult: ", measureName, unit);
        } catch (NumberFormatException e) {
            validInt("You need to type your " + measureName + " using numbers integers (" + unit + "): ", measureName, unit);
        }
        return measureNum;
    }

    private void createPlayer(String playerName, int playerAge, double playerHeight, double playerWeight) {
        player.setName(playerName);
        player.setAge(playerAge);
        player.setHeight(playerHeight);
        player.setWeight(playerWeight);
    }

    //    updates player with current game status e.g. player inventory, current room etc.
    private void gameStatus() {
        prompter.info("------------------------------");
        prompter.info("Available commands: GO <room name>, GET <item>, CONSUME <item>, SEE MAP, WORKOUT <workout name>, INSPECT ROOM");
        prompter.info("You are in the " + currentRoomName + " room.");
        prompter.info(player.toString());
        prompter.info("------------------------------");
    }

    //    main function running the game, here we call all other functions necessary to run the game
    public void playGame() throws IOException, ParseException {

        page.instructions();
        getNewPlayerInfo();
        // runs a while loop
        while (!isGameOver()) {
            gameStatus();
            promptForPlayerInput();
            if (checkGameStatus()) {
                break;
            }
        }
        gameResult();
    }

    private boolean checkGameStatus() {
        return player.isWorkoutComplete() || player.isSteroidsUsed() || player.isExhausted();
    }

    private void gameResult() {

        String result = "";
        if (player.isSteroidsUsed()) {
            result = "YOU ARE A LOSER AND A CHEATER!";
        } else if (player.isExhausted()) {
            result = "You're too tired, go home dude";
        } else if (player.isWorkoutComplete()) {
            result = "CONGRATULATIONS! YOU WORKED OUT!";
        }
        prompter.info(result);
    }

    public void promptForPlayerInput() throws IOException, ParseException {
        String command = prompter.prompt("(Hit Q to quit) What is your move? ");
        String[] commandArr = command.split(" ");
        parseThroughPlayerInput(commandArr);
    }

    public void parseThroughPlayerInput(String[] action) throws IOException, ParseException {

        List<String> actionList = Arrays.asList(action);

        String actionPrefix = "";
        String playerAction = "";

        if (actionList.size() >= 1) {
            actionPrefix = actionList.get(0);
        }
        if (actionList.size() == 2) {
            playerAction = actionList.get(1);
        } else if (actionList.size() == 3) {
            playerAction = (actionList.get(1) + " " + actionList.get(2));
        }

        validatePlayerCommands(actionPrefix.toLowerCase(), playerAction.toLowerCase());
    }

    private void validatePlayerCommands(String actionPrefix, String playerAction) throws IOException, ParseException {
        try {
            switch (actionPrefix) {
                case "get":
                    grabItem(playerAction);
                    break;
                case "go":

                    prompter.info("you're going here: " + playerAction);
                    currentRoomName = playerAction;
                    setCurrentRoom(jsonParser.getObjectFromJSONObject(rooms, playerAction));
                    break;
                case "workout":
                    playerUseMachine(playerAction);
                    break;
                case "consume":
                    if (player.consumeItem(playerAction)) {
                        player.removeItemFromInventory(playerAction);
                    }
                    break;
                case "inspect":
                    inspectRoom();
                    break;
                case "talk":
                    talkToNPC();
                    break;
                case "see":
                    getRoomMap();
                    break;
                case "q":
                    quit();
                    break;
                case "fight":
                    boxingLocation();
                    break;
            }
        } catch (Exception exception) {
//            TODO: add array with possible values for commands
            prompter.info(actionPrefix + " was sadly and invalid answer. \n please ensure you are using a valid and complete command. ");
//            TODO: fix bug caused by pressing enter where prompt for player does not work and calls inspect
            promptForPlayerInput();
        }
    }
    private void boxingLocation() {
        if (currentRoomName.equals("machine room")) {
            List<String> list = Arrays.asList("A", "B", "C", "D");
            int partnerHealth = 100;
        while (player.getHealth() > 0 && partnerHealth > 0) {
            prompter.info("Partner health: " + partnerHealth + " Your health: " + player.getHealth());
            String playerAttack = prompter.prompt("Choose your attacks: \n (A) Punch.\n (B) Kick. \n (C) BodySlam.\n (D) Open Hand smack.").toLowerCase();
            if (playerAttack.isBlank() || !playerAttack.toLowerCase().contains((CharSequence) list)) {
                prompter.info("Enter a valid command");
            }
            if (playerAttack.equals("a")) {
                prompter.info(ORANGE + "Crack! Right in the kisser!" + RESET);
                partnerHealth = partnerHealth - 25;
            }
            if (playerAttack.equals("b")) {
                prompter.info(ORANGE + "Phenomenal head kick! You may be in the wrong profession here" + RESET);
                partnerHealth = partnerHealth - 30;
            }
            if (playerAttack.equals("c")) {
                prompter.info(ORANGE + "OHHHHH Snap! You slammed your partner down!" + RESET);
                partnerHealth = partnerHealth - 40;
            }
            if (playerAttack.equals("d")) {
                prompter.info(ORANGE + "WHAP! You didn't do much damage but you certainly showed who's boss!" + RESET);
                partnerHealth = partnerHealth - 10;
            }
            Random rand = new Random();
            int randomNum = rand.nextInt((3 - 1) + 1) + 1;
            if (randomNum == 1) {
                prompter.info(RED + "Your partner backhanded you.....Disrespectful" + RESET);
                player.setHealth(player.getHealth() - 10);
            }
            if (randomNum == 2) {
                prompter.info(RED + "partner throws a nasty uppercut that connected...ouch" + RESET);
                player.setHealth(player.getHealth() - 30);
            }
            if (randomNum == 3) {
                prompter.info(RED + "OH no, your partner body slammed you into the pavement...That has to hurt" + RESET);
                player.setHealth(player.getHealth() - 40);
            }
        }
        String badge = "Medallion";
        if (player.getHealth() > partnerHealth || player.getHealth() == partnerHealth) {
            prompter.info(GREEN + "You fought like a pro !" + RESET);
            prompter.info(GREEN + "You have earned yourself a " + RESET + ORANGE + badge + RESET);
        } else {
            prompter.info(RED + "Your sparring partner  won :( " + RESET);
            prompter.info(RED + "You live to fight another day" + RESET);
            gui.clear();
//                String banner = Files.readString(Path.of("resources/loser"));
//                prompter.asciiArt(banner);
            quit();
        }
    }
        fightOver = true;
    }

    public static void setInputStream(ByteArrayInputStream inputStream) {
        Game.inputStream = inputStream;
    }

    public static boolean isItemRequired(List items) {
        return !"none".equals(items.get(0));
    }

    private void getRoomMap() throws IOException {
        currentRoom.getRoomMap(currentRoomName);
    }

    private void talkToNPC() {
        String dialog = currentRoom.getNpc().generateDialog();
        prompter.info(dialog);

        String npcItem = (String) currentRoom.npc.getInventory().get(0);

        player.getInventory().add(npcItem);
        prompter.info("You added " + npcItem + " to your gym bag.");
    }

    private void inspectRoom() {
        prompter.info(currentRoom.toString());
    }

    private void playerUseMachine(String playerExcerciseInput) {
        prompter.info("you're using the: " + playerExcerciseInput);
        Object exercises = getCurrentRoom().getExercises();

        Exercise exercise = new Exercise(exercises, playerExcerciseInput);
        Object targetMuscle = exercise.getTargetMuscles();
        String exerciseStatus = exercise.getExerciseStatus();
        Long energyCost = exercise.getEnergyCost();

        if ("fixed".equals(exerciseStatus)) {
            player.workout(targetMuscle, energyCost);
            player.subtractFromPlayerEnergy(Math.toIntExact(energyCost));
        } else {
            fixBrokenMachine(targetMuscle, energyCost);

        }
    }

    private void fixBrokenMachine(Object targetMuscle, Long energyCost) {
        if (player.getInventory().contains("wrench")) {
            String playerResponse = prompter.prompt("This machine is broken. Would you like to use your wrench to fix it? (y/n) \n >");
            if ("y".equalsIgnoreCase(playerResponse)) {
                player.getInventory().remove("wrench");
                player.workout(targetMuscle, energyCost);
                player.subtractFromPlayerEnergy(Math.toIntExact(energyCost));
            } else {
                prompter.info("When you are ready to workout, come back with the wrench and get to it.");
            }
        } else {
            prompter.info("This machine is broken, please come back with a wrench to fix it.");
        }
    }

    private void grabItem(String playerAction) {
        prompter.info("you got the :" + playerAction);
        player.getInventory().add(playerAction);
    }

    //    gives player ability to quit
    private void quit() {
        try {
            gui.clear();
            String banner = Files.readString(Path.of("resources/thankyou.txt"));
            prompter.asciiArt(banner);
            Thread.sleep(3000);
            System.exit(0);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

    }


    public static ByteArrayInputStream getInputStream() {
        return inputStream;
    }

    private void setCurrentRoom(Object currentRoom) {

        this.currentRoom = new Room(currentRoom);
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    // accessor methods

    public boolean isGameOver() {
        return isGameOver;
    }

    public int getEnergy() {
        return energy;
    }

    public int getCurrentEnergy() {
        return currentEnergy;
    }

    public String getPlayerName() {
        return playerName;
    }

}



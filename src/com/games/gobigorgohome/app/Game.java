package com.games.gobigorgohome.app;


import com.games.gobigorgohome.*;
import com.games.gobigorgohome.characters.Player;
import com.games.gobigorgohome.parsers.ParseJSON;
import com.games.gobigorgohome.parsers.ParseTxt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.games.gobigorgohome.Colors.*;


public class Game {

    public static ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes());
    private GUI gui = GUI.getInstance();
    private InputOutput prompter = new InputOutput(gui);
    private SoundPlayer soundPlayer = new SoundPlayer();
    private VoiceRecognition voiceRecognition;
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

    public Game() {
        voiceRecognition = new VoiceRecognition();
        //voiceRecognition.start();
    }

    //    collects current input from user to update their avatar
    private void getNewPlayerInfo() {
        soundPlayer.playName();
        String playerName = validString("What is your name? ", "^[a-zA-Z]{1,16}$");
        prompter.info("Hello " + playerName + " let's get more information about you...");
        soundPlayer.playHeight();
        double playerHeight = validDouble("What is your height in inches? ",
                "height", "inches", "^[0-9]{1,2}$");
        soundPlayer.playWeight();
        double playerWeight = validDouble("What is your weight in lbs? ",
                "weight", "lbs", "^[0-9]{2,3}$");
        soundPlayer.playAge();
        int playerAge = validInt("What is your age", "age", "years", "^[0-9]{1,2}$");
        createPlayer(playerName, playerAge, playerHeight, playerWeight);
    }

    // validates String using regex
    private String validString(String msg, String criteria) {
        return prompter.prompt("What is your name? ", criteria, "TRY AGAIN: " + msg);
    }

    // validates a double using regex
    private double validDouble(String msg, String measureName, String unit, String criteria) {
        return Double.parseDouble(prompter.prompt(msg, criteria, "TRY AGAIN: " + msg));
    }

    // validates int using regex
    private int validInt(String msg, String measureName, String unit, String criteria) {
        return Integer.parseInt(prompter.prompt(msg, criteria, "TRY AGAIN: " + msg));
    }

    private void createPlayer(String playerName, int playerAge, double playerHeight, double playerWeight) {
        player.setName(playerName);
        player.setAge(playerAge);
        player.setHeight(playerHeight);
        player.setWeight(playerWeight);
    }

    //    updates player with current game status e.g. player inventory, current room etc.
    private void gameStatus() {
        soundPlayer.playCommand();
        String command = voiceRecognition.getUtterance();
        prompter.info("------------------------------");
        prompter.info("Available commands: GO <room name>, GET <item>, CONSUME <item>, SEE MAP, WORKOUT <workout name>, INSPECT ROOM");
        prompter.info("You are in the " + currentRoomName + " room.");
        prompter.info(player.toString());
        prompter.info("------------------------------");
    }

    //    main function running the game, here we call all other functions necessary to run the game
    public void playGame() throws IOException, ParseException {
        soundPlayer.playIntro();
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
            // TODO play LOSER AND A CHEATER
            result = "YOU ARE A LOSER AND A CHEATER!";
        } else if (player.isExhausted()) {
            // TODO play TIRED
            result = "You're too tired, go home dude";
        } else if (player.isWorkoutComplete()) {
            // TODO play CONGRATULATIONS
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
        } catch
        (Exception exception) {
//            TODO: add array with possible values for commands
            prompter.info(actionPrefix + " was sadly and invalid answer. \n please ensure you are using a valid and complete command. ");
//            DONE: fix bug caused by pressing enter where prompt for player does not work and calls inspect - chris
            promptForPlayerInput();
        }
    }

    private void boxingLocation() {

        if (currentRoomName.equals("machines")) {
            //List<String> list = Arrays.asList("A", "B", "C", "D");

            int partnerHealth = 100;
            while (player.getHealth() > 0 && partnerHealth > 0) {
                prompter.info("Partner health: " + partnerHealth + " Your health: " + player.getHealth());
                String playerAttack = prompter.prompt("Choose your attacks: \n (A) Punch.\n (B) Kick. \n (C) BodySlam.\n (D) Open Hand smack.").toLowerCase();
//            if (!playerAttack.toLowerCase().contains((CharSequence) list)) {
//                prompter.info("Enter a valid command");
//            }
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
                prompter.info(RED + "Your sparring partner won :( " + RESET);
                prompter.info(RED + "You live to fight another day" + RESET);
                gui.clear();
//                String banner = Files.readString(Path.of("resources/loser"));
//                prompter.asciiArt(banner);
                quit();
            }
        }
        fightOver = true;
    }

    public void runAway() {
        if (currentRoomName.equals("machine room")) {

        }
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
        Long MET = exercise.getMET();
        totalCalories(MET);
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

    //MET (metabolic equivalent for task) calculation above.
    public void totalCalories(Long MET) {
        double totalBurned = 0;
        // Total calories burned = Duration (in minutes)*(MET*3.5*weight in kg)/200
        int minutes = 15;
        Double playerWeight = player.weight;
        playerWeight = playerWeight * 0.45359237; //converet lbs to KG
        totalBurned = minutes * (MET * 3.5 * playerWeight) / 200;
        totalBurned = (int) totalBurned;
        prompter.info("You burned " + totalBurned + " calories! From this workout");
    }


    //This function does not validate if item exist at the location. Refactored
//    private void grabItem(String playerAction) {
//        prompter.info("you got the :" + playerAction);
//        player.getInventory().add(playerAction);
//    }


    private void grabItem(String playerAction) {

        // makes a list to be able to manipulate data
        ArrayList<String> items = (ArrayList<String>) currentRoom.getItems();
        // check if the room has the item
        if (items.contains(playerAction)) {
            prompter.info("you got the :" + playerAction);
            player.getInventory().add(playerAction);
        } else {
            prompter.info(playerAction + " was sadly and invalid answer. \n please ensure you are using a valid and complete command. ");
            try {
                promptForPlayerInput();
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }

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
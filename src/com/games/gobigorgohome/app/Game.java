package com.games.gobigorgohome.app;


import com.games.gobigorgohome.*;
import com.games.gobigorgohome.characters.Player;
import com.games.gobigorgohome.parsers.ParseJSON;
import com.games.gobigorgohome.parsers.ParseTxt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static com.games.gobigorgohome.Colors.*;


public class Game {

    public static ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes());
    private GUI gui = GUI.getInstance();
    private InputOutput prompter = new InputOutput(gui);
    private SoundPlayer soundPlayer;
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
    private String filename = "/Images/logo.png";

    private final ParseTxt page = new ParseTxt();
    private final ParseJSON jsonParser = new ParseJSON();

//    public Game(InputOutput prompter) throws IOException, ParseException {
//        this.prompter = prompter;
//    }

    public Game() {
        soundPlayer = new SoundPlayer();
        soundPlayer.start();
        voiceRecognition = new VoiceRecognition();
        //voiceRecognition.start();
    }

    public void welcome() {
        prompter.info("<img src=\"https://res.cloudinary.com/dmrsimpky/image/upload/v1656369792/goBig_or_goHome.png\" '/>");
    }

    //    collects current input from user to update their avatar
    private void getNewPlayerInfo() {
        soundPlayer.playName();
        String playerName = validString("What is your name? ", "^[a-zA-Z]{1,16}$");
        String[] nameSoundFiles = new String[]{"chris", "david", "henwin", "manni", "renni", "scott"};
        if (Arrays.asList(nameSoundFiles).contains(playerName)) {
            soundPlayer.playSoundFile("h_" + playerName + ".wav");
        } else {
            soundPlayer.playSoundFile("h_generic.wav");
        }
        prompter.info("Hello " + CYAN + playerName + RESET + " let's get more information about you...");
        soundPlayer.playHeight();
        double playerHeight = validDouble("What is your height in inches? ",
                "height", "inches", "^[0-9]{1,2}$");
        soundPlayer.playWeight();
        double playerWeight = validDouble("What is your weight in lbs? ",
                "weight", "lbs", "^[0-9]{2,3}$");
        soundPlayer.playAge();
        int playerAge = validInt("What is your age", "age", "years", "^[0-9]{1,2}$");
        soundPlayer.playGetBig();
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
        welcome();
        //page.instructions();
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

    public void getCommands() throws IOException, ParseException {
//        soundPlayer.playIntro();
//        page.instructions();
//        getNewPlayerInfo();
        //welcome();
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

    public boolean checkGameStatus() {
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
        player.playerScore();
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
                    //condition in case room does not exist.
                    changeLocation(playerAction);
                    break;
                case "workout":
                    playerUseMachine(playerAction);
                    break;
                case "help":
                    page.instructions();
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
                    playAgain();
                    break;
                case "fight":
                    boxingLocation();
                    break;
                case "run":
                    runAway();
                    break;
            }
        } catch
        (Exception exception) {
            System.out.println(exception);
//            TODO: add array with possible values for commands
            invalidCommand(actionPrefix + " " + playerAction);
            // DONE: fix bug caused by pressing enter where prompt for player does not work and calls inspect - chris
        }
    }

    private void changeLocation(String location) {
        HashMap<String, Object> allRooms = (HashMap<String, Object>) rooms;
        Room nextRom = new Room(jsonParser.getObjectFromJSONObject(rooms, location));
        ArrayList<String> requiredItems = (ArrayList<String>) nextRom.getRequiredItems();

        if (allRooms.containsKey(location) && isItemRequired(location)) {
            setCurrentRoom(jsonParser.getObjectFromJSONObject(rooms, location));
            soundPlayer.playSoundFile("g_" + location.replaceAll(" ", "") + ".wav");
            prompter.info("you're going here: " + location);
            currentRoomName = location;
        } else {
            soundPlayer.playSoundFile("g_doesntexist.wav");
            String message = !isItemRequired(location) ? "you need " + requiredItems + " to enter " + location : "This room does not exist \n";
            invalidCommand(message);

        }
    }

    private void boxingLocation() throws IOException, ParseException {

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

            int xp = 0;
            if (player.getHealth() > partnerHealth) {
                prompter.info(GREEN + "You fought like a pro !" + RESET);
                xp++;
                prompter.info(GREEN + "You have earned yourself " + RESET + ORANGE + xp +
                        " experience point(s)" + RESET);
                prompter.info("<img src=\"https://addicted2success.com/wp-content/uploads/2013/04/Famous-Success-Quotes1.jpg\" '/>");
                promptForPlayerInput();

            } else if (player.getHealth() <= 10) {
                prompter.info(ORANGE + "Your sparring partner won :( \n You live to fight another day" + RESET);
                prompter.info("<img src=\"https://imgix.ranker.com/list_img_v2/1511/2761511/original/anime-characters-who-could-beat-goku\" " +
                        "height= 150 width= 200 '/>");
                //gui.clear();
                promptForPlayerInput();

            }
        }
        fightOver = true;
    }


    public void runAway() throws IOException, ParseException {
        if (currentRoomName.equals("machines")) {
            String fighter = prompter.prompt("Do you wish to run away or take on a fight? \n " + YELLOW +
                    "Type 'Yes' to save face or 'No' to face your fears" + RESET);
            if (fighter.equalsIgnoreCase("Yes")) {
                prompter.info(ORANGE + "Whelp, better to be safe than sorry" + RESET);
                promptForPlayerInput();
            }
            if (fighter.equalsIgnoreCase("No")) {
                prompter.info(CYAN + "Let's see what you've got!" + RESET);
                boxingLocation();
            } else {
                prompter.info("Too legit to quit!");
            }
        }
    }


    public static void setInputStream(ByteArrayInputStream inputStream) {
        Game.inputStream = inputStream;
    }

    public boolean isItemRequired(String nextLocation) {
        boolean isItRequired = false;
        Room nextRom = new Room(jsonParser.getObjectFromJSONObject(rooms, nextLocation));
        ArrayList<String> requiredItems = (ArrayList<String>) nextRom.getRequiredItems();

        if (requiredItems.get(0).equals("none") || player.getInventory().contains(requiredItems.get(0))) {
            isItRequired = true;
        }

        return isItRequired;
    }

    private void getRoomMap() throws IOException {

        currentRoom.getRoomMap(prompter);

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
            items.remove(playerAction);
            player.getInventory().add(playerAction);
        } else {
            //invalidCommand(playerAction + " was sadly and invalid answer. \n please ensure you are using a valid and complete command. ");
            prompter.info(playerAction + " was sadly and invalid answer. \n please ensure you are using a valid and complete command. ");
            try {
                promptForPlayerInput();
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }

    }

    private void invalidCommand(String command) {
        //soundPlayer.playSoundFile("invalidcommand.wav");
        prompter.info(command + " Try again with a valid command or use help command to get some help.");
        try {
            promptForPlayerInput();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public void playAgain() throws IOException, ParseException {
        String playAgain = prompter.prompt("Would you like to play again? " +
                        GREEN + " [N]ew Game " + RESET + YELLOW +
                        "[R]ematch" + RESET + RED + " [E]xit" + RESET + CYAN + " [S]ave " + RESET,
                "^[EeRrNnSs]{1}$", "Please enter 'E', 'R', 'N' or 'S'");

        if ("N".equalsIgnoreCase(playAgain)) {
            isGameOver = false;
            gui.clear();
            //currentRoom = gym.getStarterRoom();
            playGame();
        } else if ("R".equalsIgnoreCase(playAgain)) {
            gui.clear();
            currentRoom = gym.getStarterRoom();
            prompter.info("Hello " + player.getName() + YELLOW + " welcome back to goBigOrGoHome !" + RESET);
            getCommands();

        } else if ("S".equalsIgnoreCase(playAgain)) {
            player.playerScore();
            gui.clear();
            welcome();
            prompter.info("Saved!");
            prompter.info("Hello " + player.getName() + " you can resume the game you saved");
            String keepPlaying = prompter.prompt("Would you like to load your saved game?").toLowerCase();
            prompter.info(GREEN + "enter Y " + RESET + " to continue, " + RED + " N to quit the game" + RESET);
            if (keepPlaying.equalsIgnoreCase("y")) {
                getCommands();
            } else {
                playAgain();
            }

        } else {
            quit();
        }

    }

    //    gives player ability to quit
    private void quit() {
        try {
            gui.clear();
            prompter.info("<img src=\"https://res.cloudinary.com/dmrsimpky/image/upload/v1656389954/Cool_Text_-_Thank_you_for_playing_414162939030150_v7ywzk.png\" '/>");
            Thread.sleep(3000);
            System.exit(0);
        } catch (InterruptedException e) {
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
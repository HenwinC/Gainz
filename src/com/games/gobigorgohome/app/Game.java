package com.games.gobigorgohome.app;

import com.games.gobigorgohome.*;
import com.games.gobigorgohome.InputOutput.VoiceRecognitionException;
import com.games.gobigorgohome.characters.Player;
import com.games.gobigorgohome.parsers.ParseJSON;
import com.games.gobigorgohome.parsers.ParseTxt;
import com.games.gobigorgohome.voice.Speak;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.games.gobigorgohome.Colors.*;

public class Game {

    public static ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes());
    private GUI gui = GUI.getInstance();
    private InputOutput prompter = new InputOutput(gui);
    private SoundPlayer soundPlayer = new SoundPlayer();
    private int defaultVoice = 43;
    boolean isGameOver = false;
    private final Gym gym = Gym.getInstance();
    private Player player = new Player();
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
    boolean isARunner = false;

    public static ExecutorService executorService = Executors.newFixedThreadPool(2);


    public Game() {
    }

    public void welcome() throws IOException {
        prompter.info("<img src=\"https://res.cloudinary.com/dmrsimpky/image/upload/v1656369792/goBig_or_goHome.png\" '/>");
        page.welcome();
        page.instructions();
    }

    //collects current input from user to update their avatar
    private void getNewPlayerInfo() {

        try {
            getNewPlayerInfoByVoice();
        } catch (Exception e) {
            e.printStackTrace();
            prompter.setInputType("k");
            getNewPlayerInfoByKeyboard();
        }
        soundPlayer.playGetBig();
    }

    private void getNewPlayerInfoByVoice() throws VoiceRecognitionException {
        player = new Player();
        prompter.announce("Hello,<break time=\".5s\"/> I'm Alexa<break time=\".5s\"/> and welcome to go big or go home");
        String playerName = prompter.ask("What is your name");
        prompter.announce("Hello " + playerName + "<break time=\".5s\"/> let's get more information about you");
        Double playerHeight = prompter.askDouble("What is your height in inches");
        Double playerWeight = prompter.askDouble("What is your weight in lbs");
        int playerAge = prompter.askInt("What is your age");
        createPlayer(playerName, playerAge, playerHeight, playerWeight);
    }

    private void getNewPlayerInfoByKeyboard() {
        player = new Player();
        String playerName = validString("What is your name? ", "^[a-zA-Z]{1,16}$");
        double playerHeight = validDouble("What is your height in inches? ",
                "height", "inches", "^[0-9]{1,2}$");
        double playerWeight = validDouble("What is your weight in lbs? ",
                "weight", "lbs", "^[0-9]{2,3}$");

        int playerAge = validInt("What is your age", "age", "years", "^[0-9]{1,2}$");
        createPlayer(playerName, playerAge, playerHeight, playerWeight);
    }

    private void createPlayer(String playerName, int playerAge, double playerHeight, double playerWeight) {
        player = new Player();
        player.setName(playerName);
        player.setAge(playerAge);
        player.setHeight(playerHeight);
        player.setWeight(playerWeight);
    }

    // validates String using regex
    private String validString(String msg, String criteria) {
        return prompter.prompt("What is your name? ", criteria, "TRY AGAIN: " + msg);
    }

    // validates a double using regex
    private double validDouble(String msg, String measureName, String unit, String criteria) {
        return Double.parseDouble(prompter.prompt(msg, criteria, "TRY AGAIN: " + msg, 0.0));
    }

    // validates int using regex
    private int validInt(String msg, String measureName, String unit, String criteria) {
        return Integer.parseInt(prompter.prompt(msg, criteria, "TRY AGAIN: " + msg));
    }


    private void grabItem(String playerAction) {

        // makes a list to be able to manipulate data
        ArrayList<String> items = (ArrayList<String>) currentRoom.getItems();
        // check if the room has the item
        if (items.contains(playerAction)) {
            prompter.announceAndDisplay(PURPLE + "you got the :" + YELLOW + playerAction);
            items.remove(playerAction);
            player.getInventory().add(playerAction);
        } else {
            prompter.announceAndDisplay("there is no " + playerAction + " in the " + currentRoom.getRoomName());
            try {
                promptForPlayerInput();
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }

    }


    //    updates player with current game status e.g. player inventory, current room etc.
    private void gameStatus() {
        soundPlayer.playCommand();

        prompter.info(YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-");
        prompter.info(PURPLE + "Available commands:" + YELLOW + "GO " + PURPLE + "<room name>," + YELLOW + "GET " + PURPLE + "<item>, " + YELLOW + "CONSUME " + PURPLE + "<item>, " + YELLOW + "SEE MAP, " + YELLOW + "WORKOUT, " + YELLOW + "INSPECT" + RESET);
        prompter.announceAndDisplay(PURPLE + "You are in the " + RESET + YELLOW + currentRoomName + " room.");
        if (currentRoomName.equalsIgnoreCase("machines") && !isARunner) {
            prompter.announceAndDisplay(RED + "We recently added a boxing ring! " + PURPLE + "You can test out your skills by saying or entering " + YELLOW + "'Fight'" + RESET + " or 'run' if you wish to avoid conflict");
        }
        prompter.info(PURPLE + player.toString());
        prompter.info(YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-" + YELLOW + "-" + PURPLE + "-");
    }

    //    main function running the game, here we call all other functions necessary to run the game
    public void playGame() throws IOException, ParseException {

        //speak.say("Hello, I'm Alexa and welcome to go big or go home");
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
            player.playerScore();
            result = YELLOW + "CONGRATULATIONS! YOU WORKED OUT!" + RESET + "\n"
                    + "Wins: " + player.getWins() + " | Losses: " + player.getLosses();

        }
        prompter.announceAndDisplay(result);
        try {
            playAgain();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

    }

    public void promptForPlayerInput() throws IOException, ParseException {
        String command = prompter.getResponse("What is your move?");
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
        // System.out.println("validatePlayerCommands: " + actionPrefix + " " + playerAction);
        try {
            switch (actionPrefix) {
                case "get":
                    grabItem(playerAction);
                    break;
                case "go":
                    changeLocation(playerAction);
                    break;
                case "workout":
                    performWorkout();
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
                case "quit":
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
            exception.printStackTrace();
            //TODO: add array with possible values for commands
            invalidCommand(actionPrefix + " " + playerAction);
            // DONE: fix bug caused by pressing enter where prompt for player does not work and calls inspect - chris
        }
    }

    private void changeLocation(String location) {
        HashMap<String, Object> allRooms = (HashMap<String, Object>) rooms;

        if (allRooms.containsKey(location)) {
            Room nextRom = new Room(jsonParser.getObjectFromJSONObject(rooms, location));
            ArrayList<String> requiredItems = (ArrayList<String>) nextRom.getRequiredItems();

//            // System.out.println("new location: " + location + " " +
//                allRooms.containsKey(location) + " " +isItemRequired(location) +
//                " " + requiredItems);

            if (isItemRequired(location) &&
                    player.getInventory().contains(requiredItems.get(0))) {
                //   review isItemRequired(location) player.getInventory()
                setCurrentRoom(jsonParser.getObjectFromJSONObject(rooms, location));
                prompter.announceAndDisplay("ok, lets go to the " + location);
                currentRoomName = location;
            } else if (isItemRequired(location) &&
                    !player.getInventory().contains(requiredItems.get(0))) {
                String message = "you need " + requiredItems + " to enter the " + location;
                prompter.announceAndDisplay(message);
            } else {
                setCurrentRoom(jsonParser.getObjectFromJSONObject(rooms, location));
                currentRoomName = location;
            }

//        Room nextRom = new Room(jsonParser.getObjectFromJSONObject(rooms, location));
//        ArrayList<String> requiredItems = (ArrayList<String>) nextRom.getRequiredItems();
//
//        if (allRooms.containsKey(location) && isItemRequired(location)) {
//            setCurrentRoom(jsonParser.getObjectFromJSONObject(rooms, location));
//            soundPlayer.playSoundFile("g_" + location.replaceAll(" ", "") + ".wav");
//            prompter.info(PURPLE + "you're going here: " + YELLOW + location);
//            currentRoomName = location;
//
//        } else {
//            String message = location + " does not exist \n";
//            prompter.announce(message, true);
//            return;
//        }

            //} else if(isItemRequired(location)) {
            //    String message = "you need " + requiredItems + " to enter the " + location;
            //}
            //String message = !isItemRequired(location) ? "you need " + requiredItems + " to enter " + location : "This room does not exist \n";
            //prompter.announce(message,true);
            //prompter.prompt(message);
            //invalidCommand(message);
        }
    }

    private void boxingLocation() throws IOException, ParseException {
        isARunner = true;
        if (currentRoomName.equals("machines")) {
//            List<String> list = Arrays.asList("A", "B", "C", "D");

            int partnerHealth = 100;
            while (player.getHealth() > 0 && partnerHealth > 0) {
                prompter.info("Partner health: " + partnerHealth + " Your health: " + player.getHealth());

                String playerAttack = prompter.getResponse("Choose your attacks: \n   Punch.\n   Kick. \n   Body Slam.\n  Open Hand smack.").toLowerCase();
//                if (!playerAttack.toLowerCase().contains((CharSequence) list)) {
//                    prompter.announceAndDisplay("Enter a valid command");
//                }
                if (playerAttack.equals("punch")) {
                    prompter.announceAndDisplay(ORANGE + "Crack! Right in the kisser!" + RESET);
                    partnerHealth = partnerHealth - 25;
                } else if (playerAttack.equals("kick")) {
                    prompter.announceAndDisplay(ORANGE + "Phenomenal head kick! You may be in the wrong profession here" + RESET);
                    partnerHealth = partnerHealth - 30;
                } else if (playerAttack.equals("body slam")) {
                    prompter.announceAndDisplay(ORANGE + "OHHHHH Snap! You slammed your partner down!" + RESET);
                    partnerHealth = partnerHealth - 40;
                } else if (playerAttack.equals("open hand smack")) {
                    prompter.announceAndDisplay(ORANGE + "WHAP! You didn't do much damage but you certainly showed who's boss!" + RESET);
                    partnerHealth = partnerHealth - 10;
                }

                Random rand = new Random();
                int randomNum = rand.nextInt((3 - 1) + 1) + 1;
                if (randomNum == 1) {
                    prompter.announceAndDisplay(RED + "Your partner backhanded you.....Disrespectful" + RESET);
                    player.setHealth(player.getHealth() - 10);
                } else if (randomNum == 2) {
                    prompter.announceAndDisplay(RED + "partner throws a nasty uppercut that connected...ouch" + RESET);
                    player.setHealth(player.getHealth() - 30);
                } else if (randomNum == 3) {
                    prompter.announceAndDisplay(RED + "OH no, your partner body slammed you into the canvas...That has to hurt" + RESET);
                    player.setHealth(player.getHealth() - 40);
                }


            }

            int xpPoints = 0;
            if (player.getHealth() > partnerHealth) {

                prompter.announceAndDisplay(GREEN + "You fought like a pro !" + RESET);
                xpPoints++;
                prompter.announceAndDisplay(GREEN + "You have earned yourself " + RESET + ORANGE + xpPoints + " experience point(s)" + RESET);
                prompter.info("<img src=\"https://addicted2success.com/wp-content/uploads/2013/04/Famous-Success-Quotes1.jpg\" '/>");
                dropItems();
                prompter.info(PURPLE + "New items have been dropped by your enemy, don't forget to pick them up" + RESET);
                promptForPlayerInput();

            } else if (player.getHealth() <= 10) {

                prompter.announceAndDisplay(ORANGE + "Your sparring partner won :( \n You live to fight another day" + RESET);

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
            String fighter = prompter.getResponse("Do you wish to run away or take on a fight? \n " + YELLOW +
                    "'Yes' to save face or 'No' to face your fears" + RESET);
            if (fighter.equalsIgnoreCase("y")) {
                isARunner = true;
                prompter.announceAndDisplay(ORANGE + "Whelp, better to be safe than sorry" + RESET);
                promptForPlayerInput();
            } else if (fighter.equalsIgnoreCase("n")) {
                //review remove tage for speech

                prompter.announceAndDisplay("Too legit to quit!");
                prompter.announceAndDisplay(CYAN + "Let's see what you've got!" + RESET);
                isARunner = false;
                boxingLocation();
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
        // review: valid statement
        // || player.getInventory().contains(requiredItems.get(0))
        if (!requiredItems.get(0).equals("none")) {
            isItRequired = true;
        }

        return isItRequired;
    }

    private void getRoomMap() throws IOException {
        currentRoom.getRoomMap(prompter);
    }

    private void talkToNPC() {

        String dialog = currentRoom.getNpc().generateDialog(); // review change voice?
        prompter.announceAndDisplay(currentRoom.getNpc().getNpcName() + ". says: ");
        Speak.setVoice(currentRoom.getNpc().getVoiceId());
        prompter.announceAndDisplay(dialog);
        Speak.setVoice(getDefaultVoice());

        String npcItem = (String) currentRoom.npc.getInventory().get(0);

        if (!player.getInventory().contains(npcItem)) {
            player.getInventory().add(npcItem);
            prompter.announceAndDisplay("You added " + npcItem + " to your gym bag.");

        }
    }

    private void inspectRoom() {
        gui.clear();
        prompter.info(currentRoom.toString());
    }

    private void performWorkout() {
        prompter.info("======================WORKOUTS======================");
        for (Object exercise : getCurrentRoom().getExerciseList()) {
            if (exercise.toString().equals("none")) {
                prompter.info(GREEN + getCurrentRoom().getRoomName() + RESET + " is not a room where you can workout! Feel free to go to any of the workout rooms available in the GYM.");
                break;
            }
            gui.createButton(exercise.toString(), this);
        }
        prompter.info("\n======================================================");
    }

    public void playerUseMachine(String playerExcerciseInput) {
        gui.clear();
        prompter.announceAndDisplay("you're using the: " + playerExcerciseInput);
        Object exercises = getCurrentRoom().getExercises();

        Exercise exercise = new Exercise(exercises, playerExcerciseInput);

        Object targetMuscle = exercise.getTargetMuscles();

        String exerciseStatus = exercise.getExerciseStatus();
        Long energyCost = exercise.getEnergyCost();
        Long MET = exercise.getMET();


        if ("fixed".equals(exerciseStatus) || fixBrokenMachine(exerciseStatus)) {
            if (fixBrokenMachine(exerciseStatus)) {
                prompter.announceAndDisplay("You got the wrench! you did a great job fixing the machine now you can use it.");
                player.getInventory().remove("wrench");
            }
            player.workout(targetMuscle, energyCost);
            player.subtractFromPlayerEnergy(Math.toIntExact(energyCost));
            prompter.info("<img src=\"" + exercise.getExercisePicture() + "\"'/>");
            prompter.announceAndDisplay(PURPLE + "you have burned " + YELLOW + player.caloriesBurnedPerWorkout(MET) + PURPLE + " calories this workout!");
            prompter.announceAndDisplay(PURPLE + "You have burned " + YELLOW + player.totalCaloriesBurnedToday + PURPLE + " so far today!");

        }

    }

    private boolean fixBrokenMachine(String machine) {
        boolean isFixed = false;
        if (player.getInventory().contains("wrench")) {
            isFixed = true;
        } else if ("broken".equals(machine)) {
            prompter.announceAndDisplay("This machine is broken, please come back with a wrench to fix it.");
            prompter.announceAndDisplay("<img src=\"https://res.cloudinary.com/dile8hu1p/image/upload/c_scale,w_386/v1656711532/gogh/wrench_jtss1n.png\"'/>");
        }

        return isFixed;
    }


    private void dropItems() {
        ArrayList<String> items = (ArrayList<String>) currentRoom.getItems();
        items.add("pancake");
        items.add("caffeine");
        items.add("croissant");
        items.add("protein shake");

    }

    private void invalidCommand(String command) {
        String message = command + " is an invalid command. Try again or use the help command.";
        prompter.announceAndDisplay(message);
        try {
            promptForPlayerInput();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public void resetGame() {
        isGameOver = false;
        player = new Player();
        currentRoomName = gym.getStarterRoomName();
        currentRoom = new Room(currentRoomName);
        player.getEnergy();
        player.setInventory(null);
        player.setWins(0);
        gui.clear();
    }

    public void replayGame() {
        isGameOver = false;
        createPlayer(player.getName(), player.getAge(), player.getHeight(), player.getWeight());
        prompter.info("Hello " + CYAN + player.getName() + RESET + YELLOW + " welcome back to goBigOrGoHome !" + RESET);
        gui.clear();
    }


    public void playAgain() throws IOException, ParseException {
        // review
        String playAgain = null;
        prompter.announceAndDisplay("Would you like to play again? ");

        playAgain = prompter.prompt(GREEN + " [N]ew Game " + RESET + YELLOW +
                        "[R]ematch" + RESET + CYAN + " [S]ave " + RESET + RED + " [E]xit" + RESET,
                "^[EeRrNnSs]{1}$", "Please enter 'E', 'R', 'N' or 'S'");

        if ("N".equalsIgnoreCase(playAgain) || "New game".equalsIgnoreCase(playAgain)) {
            resetGame();
            playGame();
        } else if ("R".equalsIgnoreCase(playAgain) || "Rematch".equalsIgnoreCase(playAgain)) {

            gui.clear();
            currentRoom = gym.getStarterRoom();
            prompter.announceAndDisplay("Hello " + player.getName() + YELLOW + " welcome back to goBigOrGoHome !" + RESET);

            replayGame();

            getCommands();
        } else if ("S".equalsIgnoreCase(playAgain) || "save".equalsIgnoreCase(playAgain)) {
            player.playerScore();
            gui.clear();
            welcome();
            prompter.announceAndDisplay("Your game has been saved!");
            prompter.announceAndDisplay("Hello " + player.getName() + " you can resume the game you saved");
            String keepPlaying = prompter.getResponse("Would you like to load your saved game?").toLowerCase();
            prompter.announceAndDisplay(GREEN + "enter Y " + RESET + " to continue, or " + RED + " N to quit the game" + RESET);
            if (keepPlaying.equalsIgnoreCase("y")) {
                getCommands();
            } else {
                playAgain();
            }
        } else if ("e".equalsIgnoreCase(playAgain) || "exit".equalsIgnoreCase(playAgain)) {
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

    public int getDefaultVoice() {
        return defaultVoice;
    }
}

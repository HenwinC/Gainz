package com.games.gobigorgohome.client;

import com.apps.util.Prompter;
import com.games.gobigorgohome.InputOutput;
import com.games.gobigorgohome.app.Game;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, ParseException {
        Game app = new Game();
        app.playGame();
    }
}
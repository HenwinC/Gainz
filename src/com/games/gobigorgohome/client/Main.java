package com.games.gobigorgohome.client;


import com.games.gobigorgohome.app.Game;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, java.text.ParseException {
        Game app = new Game();
        app.playGame();
    }
}
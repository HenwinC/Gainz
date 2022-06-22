package com.games.gobigorgohome;

import java.awt.*;

public enum Colors {

    RESET("</span>"),
    CYAN("#00FFFF"),
    MAGENTA("#FF00FF"),
    ORANGE("#ffa500"),
    RED("#ff0000"),
    RED_UNDERLINED("RED_UNDERLINED"),
    GREEN("#00FF00"),
    YELLOW("#FFFF00"),
    DARK_YELLOW("#8B8000"),
    LIGHT_GREY("#A9ADAF"),
    WHITE("#000000"),
    BLACK("#ffffff");

    private final String color;

    Colors(String color) {
        this.color = color;
    }

    public Color getRGB() {
        return Color.decode("#FFCCEE");
    }

    public String toString() {
        return color;
    }
}
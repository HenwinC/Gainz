package com.games.gobigorgohome.parsers;

import com.games.gobigorgohome.GUI;
import com.games.gobigorgohome.InputOutput;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.games.gobigorgohome.Colors.*;


public class ParseTxt {
    public static ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes());
    private GUI gui = GUI.getInstance();
    private InputOutput prompter = new InputOutput(gui);


//    public void dataFromFile(String filePath) throws IOException {
//
//        InputStream stream = ParseTxt.class.getClassLoader().getResourceAsStream(filePath);
//        if (stream == null) {
//            throw new IllegalArgumentException("File Not Found");
//        }
//        List<String> lines = new ArrayList<String>();
//        BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream));
//        String line = null;
//        while((line=streamReader.readLine())!=null) {
//            lines.add(line);
//            prompter.info(line);
//        }
//    }

    public void dataFromFile(String filePath) throws IOException {

        String text = Files.readString(Path.of("resources/" + filePath));
        prompter.info(text);

    }

    public void instructions() throws IOException {
//        String banner = Files.readString(Path.of("resources/banner.txt"));// new banner was added
//        prompter.asciiArt(banner);
        prompter.info(YELLOW + "INSTRUCTIONS:" + RESET);
        dataFromFile("instructions2.txt");

    }

    public void welcome() throws IOException {
        prompter.info(PURPLE + "WELCOME TO 'GO BIG OR GO HOME! A game by GAINZZZ Productions" +
                " where you will learn " +
                "to use your gym time wisely and get BIG!");

        dataFromFile("instructions.txt");
    }

}



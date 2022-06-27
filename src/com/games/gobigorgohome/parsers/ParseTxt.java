package com.games.gobigorgohome.parsers;

import com.games.gobigorgohome.GUI;
import com.games.gobigorgohome.InputOutput;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


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
        //String bannerPath = "banner.txt";
        String banner = Files.readString(Path.of("resources/banner.txt"));
        prompter.asciiArt(banner);
        prompter.info("\033[33;1;2mWELCOME TO 'GO BIG OR GO HOME! v.1.1'\033[0m\nA game by \033[33;1;2mGAINZZZ Productions\033[0m where you maybe learn " +
                "to use your gym time wisely and get BIG!");
        prompter.info("\033[31;4;1mINSTRUCTIONS:\033[0m");
        dataFromFile("instructions.txt");
        prompter.info("\033[31;4;1m!!!IMPORTANT!!!:\033[0m");
        dataFromFile("instructions2.txt");

    }

}



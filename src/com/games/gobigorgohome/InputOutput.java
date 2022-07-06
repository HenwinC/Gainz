package com.games.gobigorgohome;


import com.games.gobigorgohome.app.Game;
import com.games.gobigorgohome.voice.Speak;
import com.games.gobigorgohome.voice.Transcriber;

import javax.swing.text.DefaultCaret;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import static com.games.gobigorgohome.Colors.RED;
import static com.games.gobigorgohome.Colors.RESET;


public class InputOutput {

    private GUI gui;
    private Speak speak;
    private Transcriber transcriber;
    private String inputType = "v"; //  v = voice   k = keyboard
    private Map<String, String> thesaurus = new HashMap();

    public InputOutput(GUI gui) {
        this.gui = gui;
        speak = Speak.getInstance();
        transcriber = Transcriber.getInstance();
        loadThesaurus();
    }

    public void asciiArt(String var1) {
        info("<pre>" + var1 + "</pre>");
    }

    public void scroll2bottom() {
        DefaultCaret caret = (DefaultCaret) gui.getTextPane().getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        gui.getTextPane().setCaretPosition(gui.getTextPane().getDocument().getLength());
    }

    public void info(String var1) {
        var1 = addColorTags(var1);
        var1 = var1.replaceAll("\n", "<br/>");
        var1 = var1 + "<br>";
        StyledDocument styleDoc = gui.getTextPane().getStyledDocument();
        HTMLDocument doc = (HTMLDocument) styleDoc;
        Element last = doc.getParagraphElement(doc.getLength());
        try {
            doc.insertBeforeEnd(last, var1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        scroll2bottom();
        try {
            String str = doc.getText(0, doc.getLength() - 1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getInput() {
        while (Game.inputStream == null || Game.inputStream.available() == 0) ;
        byte[] byteArray = new byte[Game.inputStream.available()];
        Game.inputStream.read(byteArray, 0, Game.inputStream.available());
        gui.getCommandInput().setPlaceholder("");
        return new String(byteArray);
    }

    public String prompt(String message) {
        gui.getCommandInput().setText("");
        String placeholderMessage = stripColorTags(message);
        gui.getCommandInput().setPlaceholder(placeholderMessage);
        info("<b>" + message + "</b>");
        return getInput();
    }

    public String prompt(String var1, String var2, String var3) {
        String var4 = prompt(var1);
        var3 = RED + var3 + RESET;
        while (!var4.matches(var2)) {
            var4 = prompt(var3);
        }
        return var4;
    }

    public String prompt(String var1, String var2, String var3, Object type) {
        String var4 = prompt(var1);
        if (type instanceof Double) {
            var4 = var4.replaceAll("[^0-9.]", "");
        }
        var3 = RED + var3 + RESET;
        while (!var4.matches(var2)) {
            var4 = prompt(var3);
        }
        info(var4);
        return var4;
    }

    public String addColorTags(String message) {
        Colors colors[] = Colors.values();
        for (Colors color : colors) {
            message = message.replaceAll(color.toString(), "<span style=\"color:" + color.toString() + ";\">");
        }
        return message;
    }

    public void announceAndDisplay(String message) {
        announce(message, true);
        info(message);
    }

    public String announce(String what) {
        return announce(what, true);
    }

    public String announce(String what, boolean block) {
        what = cleanSpeak(what);
        String result = "";
        try {
            Future<String> voiceFuture = Game.executorService.submit(new Speak.Announce(what));
            if (block) {
                result = voiceFuture.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Integer askInt(String what) {
        Integer anInt = null;
        int wrongEntryCounty = 0;
        while (anInt == null) {
            String answer = ask(what);
            answer = answer.replaceAll("[^0-9]+", "");
            try {
                anInt = Integer.parseInt(answer);
            } catch (Exception e) {
                if (wrongEntryCounty++ > 1) {
                    voiceNotUnderstood();
                    throw new VoiceRecognitionException("Integer could not be parsed from voice input");
                } else {
                    announce("lets try that again<break time=\"1s\"/>", true);
                }
            }
        }
        System.out.println("edited1: " + anInt);
        return anInt;
    }

    public Double askDouble(String what) {
        Double aDouble = null;
        int wrongEntryCounty = 0;
        while (aDouble == null) {
            String answer = ask(what);
            answer = answer.replaceAll("[^0-9.]+", "");
            try {
                aDouble = Double.parseDouble(answer);
            } catch (Exception e) {
                if (wrongEntryCounty++ > 1) {
                    voiceNotUnderstood();
                    throw new VoiceRecognitionException("Double could not be parsed from voice input");
                } else {
                    announce("lets try that again<break time=\"1s\"/>", true);
                }
            }
        }
        System.out.println("edited1: " + aDouble);
        return aDouble;
    }

    public String ask(String what) {
        announce(what, true);
        String answer = null;
        try {
            Future<String> answerFuture = Game.executorService.submit(new Transcriber.Ask());
            answer = cleanTranscribe(answerFuture.get().toLowerCase());
            System.out.println("edited0: " + answer);
        } catch (Exception e) { e.printStackTrace(); }
        return answer;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public void voiceNotUnderstood() {
        announce("lets try this another way", true);
        setInputType("k");
    }

    public class VoiceRecognitionException extends RuntimeException {
        public VoiceRecognitionException() {
            super();
        }

        public VoiceRecognitionException(String errorMessage) {
            super(errorMessage + " Voice input could not be recognized." +
                    " Make sure a microphone is available");
        }
    }

    public String getResponse(String message) {
        if (getInputType().equals("v")) {
            return ask(message);
        }
        return prompt(message);
    }

    public String cleanSpeak(String message) {
        message = stripColorTags(message);
        message = message.replaceAll("\\<.*?\\>", "");
        message = message.replaceAll("[^\\w,'\\\"\\.{} ]+", "");
        message = message.replaceAll("[,\\!\\.]+", "<break time=\".5s\"/>");
        message = message.replaceAll("[{]+", "<prosody rate=\"125%\">")
                .replaceAll("[}]+", "</prosody>");
        //message = message.replaceAll("[\\]]+", "</prosody>");
        message = "<speak>" + message + "</speak>";
        System.out.println(message);
        return message;
    }

    public String stripColorTags(String message) {
        Colors colors[] = Colors.values();
        for (Colors color : colors) {
            message = message.replaceAll(color.toString(), "");
        }
        return message;
    }

    public String cleanTranscribe(String voiceInput) {
        voiceInput = voiceInput.toLowerCase();
        voiceInput = voiceInput.replace("[^\\w ]", "");
        voiceInput = voiceInput.replace(".", "");
        voiceInput = voiceInput.replace("'", "");
        voiceInput = checkForSynonyms(voiceInput);
        return voiceInput;
    }

    public String checkForSynonyms(String voiceInput) {
        for (Map.Entry<String, String> entry : thesaurus.entrySet()) {
            if (voiceInput.contains(entry.getKey())) {
                voiceInput = voiceInput.replace(entry.getKey(),entry.getValue());entry.getValue();
            }
        }
        return voiceInput;
    }

    public void loadThesaurus() {
        thesaurus.put("yes", "y");
        thesaurus.put("no", "n");
        thesaurus.put("zero", "0");
        thesaurus.put("one", "1");
        thesaurus.put("two", "2");
        thesaurus.put("three", "3");
        thesaurus.put("four", "4");
        thesaurus.put("five", "5");
        thesaurus.put("six", "6");
        thesaurus.put("seven", "7");
        thesaurus.put("eight", "8");
        thesaurus.put("nine", "9");
        thesaurus.put("david", "daveed");
        thesaurus.put("work out", "workout");
        thesaurus.put("go to the", "go");
        thesaurus.put("go to", "go");
        thesaurus.put("goto", "go");
        thesaurus.put("yogastudio", "yoga studio");
        thesaurus.put("w8s", "weights");

    }
}
package com.games.gobigorgohome;


import com.games.gobigorgohome.app.Game;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;
import static com.games.gobigorgohome.Colors.*;



public class InputOutput {

    private GUI gui;

    public InputOutput(GUI gui) {
        this.gui = gui;
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

    public String addColorTags(String message) {
        Colors colors[] = Colors.values();
        for(Colors color: colors) {
            message = message.replaceAll(color.toString(),"<span style=\"color:"+color.toString()+";\">");
        }
        return message;
    }

    public String stripColorTags(String message) {
        Colors colors[] = Colors.values();
        for(Colors color: colors) {
            message = message.replaceAll(color.toString(),"");
        }
        return message;
    }
}
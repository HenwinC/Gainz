package com.games.gobigorgohome;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;

public class TextFieldPlaceholder extends JTextField {

    private String placeholder;

    public TextFieldPlaceholder() {
    }

    public TextFieldPlaceholder(
            Document document,
            String text,
            int columns) {
        super(document, text, columns);
    }

    public TextFieldPlaceholder(int columns) {
        super(columns);
    }

    public TextFieldPlaceholder(String text) {
        super(text);
    }

    public TextFieldPlaceholder(String text, int columns) {
        super(text, columns);
    }

    public String getPlaceholder() {
        return placeholder;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        if (placeholder == null || placeholder.length() == 0 || getText().length() > 0) {
            return;
        }

        final Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(getDisabledTextColor());
        graphics2D.drawString(placeholder, getInsets().left, graphics.getFontMetrics()
                .getMaxAscent() + getInsets().top);
    }

    public void setPlaceholder(String placeHolder) {
        placeholder = placeHolder;
        repaint();
    }
}
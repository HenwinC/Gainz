package com.games.gobigorgohome;

import com.games.gobigorgohome.app.Game;


import javax.swing.*;
import java.awt.*;

import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.PrintStream;

public class GUI {

    private PrintStream printStream;
    private int boardWidth = 800;
    private int boardHeight = 800;
    private JFrame frame;
    private JButton resetButton;
    private JTextPane textPane;
    private TextFieldPlaceholder commandInput;
    private JTextField clockText;
    private String playerName = "you";
    private long time = 0;
//    private Clock clock;
    private Font sysOutTextFont = new Font("SansSerif", Font.BOLD, 12);
    private Font inputTextFont = new Font("SansSerif", Font.BOLD, 35);
    private Colors sysOutColorBG = Colors.LIGHT_GREY;
    private Color commandInputColorBG = Color.LIGHT_GRAY;
    private Color clockColorBG = Color.darkGray;
    //private Player player;
    private ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes());
    private SoundPlayer soundPlayer = new SoundPlayer();
    private SoundPlayer soundEffectPlayer = new SoundPlayer();
    private Color textPaneBg = new Color(106, 105, 111);
    private JButton button;
    private static GUI instance;

    public GUI() {
        createBoard();
        clear();
    }

    public static GUI getInstance() {
        if (instance == null) {
            instance = new GUI();
        }
        return instance;
    }

//    public GUI(Player player) {
//        this.player = player;
//    }

    public void clear() {
        textPane.setText("<html><head><style>body{font-size:18px;margin:10px;width:100%;text-align:left;}</style></head><body><div id=\"content\"></div></body></html>");
    }

    public void createBoard() {

        frame = new JFrame("goBigORgohome");

        frame.setSize(new Dimension(boardWidth, boardHeight));

        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container pane = frame.getContentPane();
        pane.setLayout(new GridBagLayout());
        pane.setSize(boardWidth, boardHeight);




        textPane = new JTextPane();
        textPane.setContentType("text/html;charset=UTF-16");
        textPane.setEditable(false);
        textPane.setBackground(textPaneBg);
        textPane.setFont(sysOutTextFont);
        textPane.setText("<html><head><style>body{width:100%;text-align:left;}</style></head><body><div id=\"content\"></div></body></html>");
        attachVolumeControls(textPane);

        GridBagConstraints sysOutScrollPaneConstraints = new GridBagConstraints();
        sysOutScrollPaneConstraints.fill = GridBagConstraints.BOTH;
        sysOutScrollPaneConstraints.gridx = 0;
        sysOutScrollPaneConstraints.gridy = 0;
        sysOutScrollPaneConstraints.weighty = 1.00;
        sysOutScrollPaneConstraints.gridwidth = 2;

        JScrollPane sysOutScrollPane = new JScrollPane(textPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sysOutScrollPane.setMinimumSize(new Dimension(800, 600));
        sysOutScrollPane.setMaximumSize(new Dimension(800, 600));
        sysOutScrollPane.setSize(800, 600);
        sysOutScrollPane.setBackground(new java.awt.Color(Color.darkGray.getRGB()));
        pane.add(sysOutScrollPane, sysOutScrollPaneConstraints);

        commandInput = new TextFieldPlaceholder(50);
        commandInput.setMinimumSize(new Dimension(400, 45));
        commandInput.setMaximumSize(new Dimension(400, 45));
        commandInput.setFont(inputTextFont);
        GridBagConstraints commandInputConstraints = new GridBagConstraints();
        commandInputConstraints.fill = GridBagConstraints.BOTH;
        commandInputConstraints.gridx = 0;
        commandInputConstraints.gridy = 1;
        commandInputConstraints.weightx = 1;

        attachVolumeControls(commandInput);
        commandInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == 10) {
                    String input = commandInput.getText();
                    Game.setInputStream(new ByteArrayInputStream(input.getBytes()));
                    commandInput.setText("");
                }
            }
        });
        pane.add(commandInput, commandInputConstraints);


        frame.setVisible(true);
        commandInput.requestFocus();
    }

    public void createButton(String label,Game currentGame){
        JButton button = new JButton(label);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentGame.playerUseMachine(label);
            }
        });
        textPane.insertComponent(button);
    }

    public JTextPane getTextPane() {
        return textPane;
    }

//    public void setTextPane(JTextPane textPane) {
//        this.textPane = textPane;
//    }

    public TextFieldPlaceholder getCommandInput() {
        return commandInput;
    }

//    public void setCommandInput(TextFieldPlaceholder commandInput) {
//        this.commandInput = commandInput;
//    }
//
//    public void playSoundEffect(String name) {
//        soundEffectPlayer.addSoundFile(name);
//        soundEffectPlayer.start();
//    }
//
//    public Image getImageFile(String fileName) {
//        try {
//            return ImageIO.read(getResourceFile(fileName));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

//    public void startClock() {
//        if (clock == null)
//            clock = new Clock();
//        clock.start();
//    }
//
//    public void stopClock() {
//        clock.clockRunning = false;
//        clock = null;
//    }

//    public void resetClock() {
//        stopClock();
//        time = 0;
//    }

//    class Clock extends Thread {
//
//        public boolean clockRunning = true;
//
//        @Override
//        public void run() {
//            while (clockRunning) {
//                try {
//                    Thread.sleep(1000);
//                    time++;
//                    if (time > 3600) {
//                        time = 0;
//                    }
//                    String timeString = String.format("%02d:%02d", time / 60, time % 60);
//                    clockText.setText(timeString);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

//    public File getResourceFile(String fileName) {
//        return new File("resources/" + fileName);
//    }

    public void attachVolumeControls(JComponent component) {
        component.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    soundPlayer.raiseVolume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    soundPlayer.lowerVolume();
                } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    soundPlayer.mute();
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    soundPlayer.unMute();
                }
            }
        });
    }
}
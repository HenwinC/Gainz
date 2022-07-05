package com.games.gobigorgohome.voice;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javazoom.jl.player.advanced.PlaybackListener;

public class Speak extends Thread {

    private static Creds creds = Creds.getInstance();
    private static AdvancedPlayer player = null;
    private static Voice voice;
    private static PollyClient polly;
    private static InputStream stream;
    private static List<String> sayList = Collections.synchronizedList(new ArrayList<>());
    private static boolean speaking = false;
    private static ExecutorService executor = Executors.newFixedThreadPool(1);
    private static Speak instance;
    private static boolean running = false;

    public static class Announce implements Callable<String>{

        private final String whatToSay;

        public Announce(String whatToSay) {
            this.whatToSay = whatToSay;
        }

        public String call() {
            try {
                while (!running) {
                    Thread.sleep(100);
                }
                say(whatToSay);
                while(speaking);
            } catch(Exception e) { e.printStackTrace(); }
            return "speak success";
        }
    }

    public static Speak getInstance() {
        if (instance == null) {
            instance = new Speak();
            instance.start();
        }
        return instance;
    }

    public void run() {
        try {
            init();
            System.out.println("Speak init completed ");
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void init() {

        /*AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                "AKIA42WOFZ4OV6PIFE3R",
                "ytTdAww913dDayLfySmN7Dg9OjYK92PXjwsLR2xJ");*/

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                creds.getAccessKeyId(),creds.getSecretAccessKey());

        polly = PollyClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                //.credentialsProvider(ProfileCredentialsProvider.create())
                .build();

        try {
            DescribeVoicesRequest describeVoiceRequest = DescribeVoicesRequest.builder()
                    .engine("standard")
                    .languageCode("en-US")
                    .build();

            DescribeVoicesResponse describeVoicesResult = polly.describeVoices(describeVoiceRequest);
            voice = describeVoicesResult.voices().get(1);

            String vvoice = describeVoicesResult.voices().get(1).name();
            System.out.println("vvoice: " + vvoice);

            running = true;

        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void say(String what) {
        try {
            speaking = true;
            stream = synthesize(polly, what, voice, OutputFormat.MP3);
            player = new AdvancedPlayer(stream, javazoom.jl.player.FactoryRegistry.systemRegistry().createAudioDevice());
            player.setPlayBackListener(new PlaybackListener() {
                public void playbackStarted(PlaybackEvent evt) {
                    System.out.println("Playback started: " + what);
                }
                public void playbackFinished(PlaybackEvent evt) {
                    speaking = false;
                }
            });
            player.play();
        } catch (PollyException | JavaLayerException | IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public static InputStream synthesize(PollyClient polly, String text, Voice voice, OutputFormat format) throws IOException {

        SynthesizeSpeechRequest synthReq = SynthesizeSpeechRequest.builder()
                .text(text)
                .textType(TextType.SSML) //TextType.SSML TextType.TEXT
                .engine(Engine.NEURAL)
                .voiceId(voice.id())
                .outputFormat(format)
                .build();

        ResponseInputStream<SynthesizeSpeechResponse> synthRes = polly.synthesizeSpeech(synthReq);
        return synthRes;
    }

    public static boolean isSpeaking() {
        return speaking;
    }
}
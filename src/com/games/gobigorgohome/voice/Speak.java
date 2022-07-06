package com.games.gobigorgohome.voice;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
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

import static software.amazon.awssdk.services.polly.model.Engine.STANDARD;

public class Speak extends Thread {

    private static Creds creds = Creds.getInstance();
    private static AdvancedPlayer player = null;
    private static Voice voice;
    private static PollyClient polly;
    private static DescribeVoicesResponse describeVoicesResponse;
    private static InputStream stream;
    private static List<String> sayList = Collections.synchronizedList(new ArrayList<>());
    private static boolean speaking = false;
    private static ExecutorService executor = Executors.newFixedThreadPool(1);
    private static Speak instance;
    private static boolean running = false;

    public static class Announce implements Callable<String> {

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
                while (speaking) ;
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void init() {

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                creds.getAccessKeyId(), creds.getSecretAccessKey());

        polly = PollyClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();

        try {
            DescribeVoicesRequest describeVoiceRequest = DescribeVoicesRequest.builder()
                    .engine("standard")
                    //.languageCode("en-US")
                    .build();

            describeVoicesResponse = polly.describeVoices(describeVoiceRequest);
            //voice = describeVoicesResponse.voices().get(1);
            setVoice(43);

            running = true;

            listVoices();

        } catch (Exception e) {
            e.printStackTrace();
        }
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

        System.out.println("============ " + voice.id());
        Engine ENGINE = Engine.NEURAL;
        if(voice.idAsString().equals("Russell")) {
            ENGINE = Engine.STANDARD;
        }

        SynthesizeSpeechRequest synthReq = SynthesizeSpeechRequest.builder()
                .text(text)
                .textType(TextType.SSML) //TextType.SSML TextType.TEXT
                .engine(ENGINE)
                .voiceId(voice.id())
                .outputFormat(format)
                .build();

        ResponseInputStream<SynthesizeSpeechResponse> synthRes = polly.synthesizeSpeech(synthReq);
        return synthRes;
    }

    public static String getVoice() {
        return voice.id().toString();
    }

    public static void setVoice(int voiceId) {
        voice = describeVoicesResponse.voices().get(voiceId);
        System.out.println("voice: " + voice.name());
        // 43=Joanna 40=Maintenance Lady
    }

    public static void listVoices() {
        List<Voice> voices = describeVoicesResponse.voices();
        for(int i=0;i<voices.size();i++) {
            System.out.println("" + i + " " + voices.get(i).name() + " " + voices.get(i).id());
        }
    }

    public static boolean isSpeaking() {
        return speaking;
    }
}
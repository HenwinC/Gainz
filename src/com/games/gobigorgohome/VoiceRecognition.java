package com.games.gobigorgohome;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.Microphone;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;

public class VoiceRecognition extends Thread {

    private Configuration configuration;
    private Microphone micro;
    private StreamSpeechRecognizer recognizer;
    private boolean record = true;

    public VoiceRecognition() {
        try {
            configuration = new Configuration();
            configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
            configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
            configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
            micro = new Microphone(16000, 16, true, false);

//            micro.startRecording();
//            recognizer = new StreamSpeechRecognizer(
//                    configuration);
//            recognizer.startRecognition(micro.getStream());
            /*while(true) {
                String utterance = recognizer.getResult().getHypothesis();
                System.out.println(utterance);
                if (utterance.contains("hi")
                        || utterance.contains("hello")) {
                    break;
                }
            }*/
//            recognizer.stopRecognition();
//            micro.stopRecording();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            System.out.println("started voice recognition...");
            micro.startRecording();
            recognizer = new StreamSpeechRecognizer(
                    configuration);
            recognizer.startRecognition(micro.getStream());
            while (record) {
                String utterance = recognizer.getResult().getHypothesis();
                System.out.println(utterance);
            }
            recognizer.stopRecognition();
            micro.stopRecording();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUtterance() {
//        recognizer.startRecognition(micro.getStream());
//        String result = recognizer.getResult().getHypothesis();
//        recognizer.stopRecognition();
//        micro.stopRecording();

        return "";

    }
}
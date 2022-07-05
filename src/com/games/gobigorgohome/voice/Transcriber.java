package com.games.gobigorgohome.voice;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.transcribestreaming.TranscribeStreamingAsyncClient;
import software.amazon.awssdk.services.transcribestreaming.model.*;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class Transcriber extends Thread {

    private static final Region REGION = Region.US_EAST_1;
    private static Creds creds = Creds.getInstance();
    private static Transcriber instance;
    private static TargetDataLine line;
    private static boolean running = false;
    private static TranscribeStreamingAsyncClient client;
    private static ExecutorService executor = Executors.newFixedThreadPool(1);
    private static List<String> transcribedList = Collections.synchronizedList(new ArrayList<>());
    private static final AtomicBoolean asking = new AtomicBoolean(false);

    public static class Ask implements Callable {

        public Ask() {
        }

        public String call() {
            String result = "";
            try {
                asking.compareAndSet(false, true);
                while (!running || transcribedList.isEmpty()) {
                    Thread.sleep(50);
                }
                result = transcribedList.get(0);
                transcribedList.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
    }

    public static Transcriber getInstance() {
        if (instance == null) {
            instance = new Transcriber();
            instance.start();
        }
        return instance;
    }

    public void run() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() {
        try {
            AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                    creds.getAccessKeyId(), creds.getSecretAccessKey());

            client = TranscribeStreamingAsyncClient.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                    .region(REGION)
                    .build();

            running = true;

            CompletableFuture<Void> result = client.startStreamTranscription(getRequest(16_000),
                    new AudioStreamPublisher(getStreamFromMic()), getResponseHandler());

            System.out.println("Transcribe init completed ");

            result.get();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static InputStream getStreamFromMic() throws LineUnavailableException {

        int sampleRate = 16000;
        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Line not supported");
            System.exit(0);
        }

        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        InputStream audioStream = new AudioInputStream(line);
        return audioStream;
    }

    private static AwsCredentialsProvider getCredentials() {
        return DefaultCredentialsProvider.create();
    }

    private static StartStreamTranscriptionRequest getRequest(Integer mediaSampleRateHertz) {
        return StartStreamTranscriptionRequest.builder()
                .languageCode(LanguageCode.EN_US.toString())
                .mediaEncoding(MediaEncoding.PCM)
                .mediaSampleRateHertz(mediaSampleRateHertz)
                .build();
    }

    private static StartStreamTranscriptionResponseHandler getResponseHandler() {

        return StartStreamTranscriptionResponseHandler.builder()
                .onResponse(r -> {
                    System.out.println("we are live...");
                })
                .onError(e -> {
                    System.out.println(e.getMessage());
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    System.out.println("Error Occurred: " + sw.toString());
                })
                .onComplete(() -> {
                    System.out.println("we are offline...");
                })
                .subscriber(event -> {
                    List<Result> results = ((TranscriptEvent) event).transcript().results();
                    //System.out.println(((TranscriptEvent) event).sdkEventType());
                    //System.out.println("size: " + results.size() + " " + ((TranscriptEvent) event).transcript().results().size());
                    if (results.size() > 0) {
                        if (!results.get(0).alternatives().get(0).transcript().isEmpty() && !results.get(0).isPartial()) {
                            if (asking.get()) {
                                asking.compareAndSet(true, false);
                                String response = results.get(0).alternatives().get(0).transcript();
                                //Game.setInputStream(new ByteArrayInputStream(response.getBytes()));
                                System.out.println("said: " + response);
                                transcribedList.add(response);
                            }
                        }
                    }
                })
                .build();
    }

    private static class AudioStreamPublisher implements Publisher<AudioStream> {

        private final InputStream inputStream;
        private static Subscription currentSubscription;

        private AudioStreamPublisher(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void subscribe(Subscriber<? super AudioStream> s) {

            if (this.currentSubscription == null) {
                this.currentSubscription = new SubscriptionImpl(s, inputStream);
            } else {
                this.currentSubscription.cancel();
                this.currentSubscription = new SubscriptionImpl(s, inputStream);
            }
            s.onSubscribe(currentSubscription);
        }
    }

    public static class SubscriptionImpl implements Subscription {

        private static final int CHUNK_SIZE_IN_BYTES = 1024 * 4;
        private final Subscriber<? super AudioStream> subscriber;
        private final InputStream inputStream;
        private AtomicLong demand = new AtomicLong(0);

        SubscriptionImpl(Subscriber<? super AudioStream> s, InputStream inputStream) {
            this.subscriber = s;
            this.inputStream = inputStream;
        }

        @Override
        public void request(long n) {
            if (n <= 0) {
                subscriber.onError(new IllegalArgumentException("Demand must be positive"));
            }

            demand.getAndAdd(n);

            executor.submit(() -> {
                try {
                    System.out.println("executorexecutorexecutorexecutor ");
                    do {
                        ByteBuffer audioBuffer = getNextEvent();
                        if (!asking.get()) {
                            audioBuffer = ByteBuffer.allocate(1000);
                        }
                        if (audioBuffer.remaining() > 0) {
                            AudioEvent audioEvent = audioEventFromBuffer(audioBuffer);
                            subscriber.onNext(audioEvent);
                        } else {
                            subscriber.onComplete();
                            break;
                        }
                    } while (demand.decrementAndGet() > 0);
                } catch (Exception e) {
                    subscriber.onError(e);
                }
                System.out.println("end end end end");
            });
        }

        @Override
        public void cancel() {
            executor.shutdown();
        }

        private ByteBuffer getNextEvent() {
            ByteBuffer audioBuffer = null;
            byte[] audioBytes = new byte[CHUNK_SIZE_IN_BYTES];

            int len = 0;
            try {
                len = inputStream.read(audioBytes);

                if (len <= 0) {
                    audioBuffer = ByteBuffer.allocate(0);
                } else {
                    audioBuffer = ByteBuffer.wrap(audioBytes, 0, len);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            return audioBuffer;
        }

        private AudioEvent audioEventFromBuffer(ByteBuffer bb) {
            return AudioEvent.builder()
                    .audioChunk(SdkBytes.fromByteBuffer(bb))
                    .build();
        }
    }
}
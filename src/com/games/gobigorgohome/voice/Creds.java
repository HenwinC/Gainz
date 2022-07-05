package com.games.gobigorgohome.voice;

public class Creds {

    private static Creds instance;
    private static String accessKeyId = "AKIA42WOFZ4OV6PIFE3R";
    private static String secretAccessKey = "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";

    public static Creds getInstance() {
        if (instance == null) {
            instance = new Creds();
        }
        return instance;
    }

    public static String getAccessKeyId() {
        return accessKeyId;
    }

    public static void setAccessKeyId(String accessKeyId) {
        Creds.accessKeyId = accessKeyId;
    }

    public static String getSecretAccessKey() {
        return secretAccessKey;
    }

    public static void setSecretAccessKey(String secretAccessKey) {
        Creds.secretAccessKey = secretAccessKey;
    }
}
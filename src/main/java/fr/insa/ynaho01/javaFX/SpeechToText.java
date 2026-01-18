/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.insa.ynaho01.javaFX;

/**
 *
 * @author ihssa
 */
//import org.vosk.LibVosk;
import projectmessagerie.*;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.json.JSONObject;

import javax.sound.sampled.*;

public class SpeechToText {

    private TargetDataLine microphone;
    private Recognizer recognizer;
    private Thread recordingThread;
    private volatile boolean recording = false;

    /**
     * Enregistre 'durationSeconds' secondes au micro et renvoie la
     * transcription en texte.
     *
     * @param durationSeconds
     * @return
     * @throws java.lang.Exception
     */
    public SpeechToText() {

        this.microphone = microphone;
        this.recognizer = recognizer;
        this.recording = false;
        this.recordingThread = recordingThread;
    }

    public void startRecording() throws Exception {
        Model model = new Model("C:\\SYSTEMES MULTI TACHES\\projetMessagerie\\models\\vosk-model-small-fr-0.22");
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();

        recognizer = new Recognizer(model, 16000.0f);
        recording = true;
        recordingThread = new Thread(() -> {
            byte[] buffer = new byte[4096];
            while (recording) {
                int numBytesRead = microphone.read(buffer, 0, buffer.length);
                if (numBytesRead > 0) {
                    recognizer.acceptWaveForm(buffer, numBytesRead);
                }
            }
        });
        recordingThread.start();

    }

    public String stopRecording() {
        recording = false;
        microphone.stop();
        microphone.close();

        String resultJson = recognizer.getFinalResult();
        return new JSONObject(resultJson).optString("text", "").trim();
    }

    public TargetDataLine getMicrophone() {
        return this.microphone;
    }

    public Recognizer getRecognizer() {
        return this.recognizer;
    }

    public boolean getRecording() {
        return this.recording;
    }

    public static String recordAndTranscribe(int durationSeconds) throws Exception {

        try (Model model = new Model("C:\\SYSTEMES MULTI TACHES\\projetMessagerie\\models\\vosk-model-small-fr-0.22");) {

            // format audio demandé par Vosk : 16kHz, 16 bits, mono
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                throw new LineUnavailableException("Micro non supporté pour ce format");
            }

            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            byte[] buffer = new byte[4096];

            try (Recognizer recognizer = new Recognizer(model, 16000.0f)) {
                long endTime = System.currentTimeMillis() + durationSeconds * 1000L;

                System.out.println("🎙 Parlez maintenant pendant " + durationSeconds + "s...");

                while (System.currentTimeMillis() < endTime) {
                    int numBytesRead = microphone.read(buffer, 0, buffer.length);
                    if (numBytesRead > 0) {
                        recognizer.acceptWaveForm(buffer, numBytesRead);
                    }
                }

                microphone.stop();
                microphone.close();

                String resultJson = recognizer.getFinalResult();
                JSONObject obj = new JSONObject(resultJson);
                String text = obj.optString("text", "").trim();

                System.out.println(" Transcription : " + text);
                return text;
            }
        }
    }

}

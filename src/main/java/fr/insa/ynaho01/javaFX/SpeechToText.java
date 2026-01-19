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
    private Model model;

    private Thread recordingThread;
    private volatile boolean recording = false;

    public void startRecording() throws Exception {
        if (recording) return; // évite double start

        // charge le modèle une seule fois 
        if (model == null) {
            model = new Model("C:\\SYSTEMES MULTI TACHES\\projetMessagerie\\models\\vosk-model-small-fr-0.22");
        }

        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Micro non supporté pour ce format");
        }

        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();

        recognizer = new Recognizer(model, 16000.0f);

        recording = true;
        recordingThread = new Thread(() -> {
            byte[] buffer = new byte[4096];
            while (recording) {
                int n = microphone.read(buffer, 0, buffer.length);
                if (n > 0) {
                    recognizer.acceptWaveForm(buffer, n);
                }
            }
        }, "Vosk-Recorder");
        recordingThread.setDaemon(true);
        recordingThread.start();
    }

    public String stopRecording() {
        if (!recording) return "";

        //stop le thread proprement
        recording = false;
        try {
            if (recordingThread != null) recordingThread.join(500); // attend qu'il sorte de la boucle
        } catch (InterruptedException ignored) {}
        try {
            if (microphone != null) {
                microphone.stop();
                microphone.close();
            }
        } catch (Exception ignored) {}
        
        String text = "";
        try {
            if (recognizer != null) {
                String resultJson = recognizer.getFinalResult();
                text = new org.json.JSONObject(resultJson).optString("text", "").trim();
                recognizer.close();
            }
        } catch (Exception ignored) {}

        recognizer = null;
        microphone = null;
        recordingThread = null;
        return text;
    }

    public void closeModel() {
        try { if (model != null) model.close(); } catch (Exception ignored) {}
        model = null;
    }
}

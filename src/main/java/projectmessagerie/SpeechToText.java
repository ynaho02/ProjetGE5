/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package projectmessagerie;

/**
 *
 * @author ihssa
 */

//import org.vosk.LibVosk;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.json.JSONObject;

import javax.sound.sampled.*;

public class SpeechToText {

    // ⚠ ADAPTE CE CHEMIN à l'endroit où tu as mis le modèle
    private static final String MODEL_PATH =
            "C:/Users/ihssa/OneDrive/Bureau/ge5/PROJET IA/ProjetGE5/models/vosk-model-small-fr-0.22";

    /**
     * Enregistre 'durationSeconds' secondes au micro et renvoie la transcription en texte.
     * @param durationSeconds
     * @return 
     * @throws java.lang.Exception 
     */
    public static String recordAndTranscribe(int durationSeconds) throws Exception {
        // couper les logs Vosk (optionnel)
        
       // LibVosk.setLogLevel(LibVosk);
        

        try (Model model = new Model(MODEL_PATH)) {

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
    public static void main(String[] args) {
        try {
            String result = recordAndTranscribe(10);
            System.out.println(" Résultat final : " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


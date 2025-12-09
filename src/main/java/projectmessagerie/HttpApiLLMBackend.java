/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package projectmessagerie;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpApiLLMBackend {

    private final String apiKey;
    private final String apiUrl;

    public HttpApiLLMBackend(String apiKey, String apiUrl) {
        this.apiKey = "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";
        this.apiUrl = "https://api.openai.com/v1/responses";
    }

   public String generateSummary(String prompt) {

    // --- 1) Nettoyage du prompt pour JSON ---
    String safePrompt = prompt
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n");

    // --- 2) Corps JSON conforme à l’API RESPONSES ---
    String body = """
    {
        "model": "gpt-4o-mini",
        "instructions": "Tu es un assistant expert en résumé de réunions. Fournis un résumé clair, structuré et concis.",
        "input": "%s"
    }
    """.formatted(safePrompt);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))  
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

    try {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        String json = response.body();

        // --- Debug : afficher la réponse brute ---
        //System.out.println("Réponse OpenAI JSON : " + json);

        // --- 3) Gestion d'une éventuelle erreur API ---
         if (json.contains("\"error\"")) {
            return "Erreur LLM (rate limit ou autre).";
        }

         // 4) On cherche d'abord "type":"output_text"
        int typeIdx = json.indexOf("\"type\":\"output_text\"");
        if (typeIdx == -1) {
            return "Erreur : pas de bloc output_text dans la réponse LLM.";
        }

        // À partir de là, on cherche "text":
        int textIdx = json.indexOf("\"text\":", typeIdx);
        if (textIdx == -1) {
            return "Erreur : champ \"text\" introuvable après output_text.";
        }

        String sub = json.substring(textIdx + "\"text\":".length());

        // premier guillemet ouvrant
        int startQuote = sub.indexOf('"');
        if (startQuote == -1) {
            return "Erreur : format texte inattendu.";
        }
        sub = sub.substring(startQuote + 1);

        // guillemet fermant
        int endQuote = sub.indexOf('"');
        if (endQuote == -1) {
            return "Erreur : format texte tronqué.";
        }

        String extracted = sub.substring(0, endQuote);

        // on remet les \n en vrais retours à la ligne
        extracted = extracted.replace("\\n", "\n");

        return extracted.trim();

    } catch (IOException | InterruptedException e) {
        e.printStackTrace();
        return "Erreur résumé (exception) : " + e.getMessage();
    }
}


}


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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HttpApiLLMBackend {

    private final String apiKey;
    private final String apiUrl;
  
    
    public HttpApiLLMBackend() {
         // 1) Essayer variable d'environnement
        String key = System.getenv("OPENAI_API_KEY");

        // 2) Sinon, essayer -DOPENAI_API_KEY=...
        if (key == null || key.isBlank()) {
            key = System.getProperty("OPENAI_API_KEY");
        }

        // 3) Si toujours pas de clé → erreur claire
        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                "OPENAI_API_KEY absente. " +
                "Définis-la via variable d'environnement ou VM option -DOPENAI_API_KEY"
            );
        }
        this.apiKey = key;
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

        String jsonStr = response.body();

        // Parsing JSON propre
        JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();

        // Gestion des vraies erreurs
        if (!json.get("error").isJsonNull()) {
            return "Erreur LLM : " + json.get("error").toString();
        }

        // Aller chercher le texte dans json.output[0].content[0].text
        JsonArray output = json.getAsJsonArray("output");
        JsonObject msg = output.get(0).getAsJsonObject();

        JsonArray contentArr = msg.getAsJsonArray("content");
        JsonObject contentObj = contentArr.get(0).getAsJsonObject();

        String text = contentObj.get("text").getAsString();

        return text;

    } catch (Exception e) {
        e.printStackTrace();
        return "Erreur résumé : " + e.getMessage();
    }
   }


}


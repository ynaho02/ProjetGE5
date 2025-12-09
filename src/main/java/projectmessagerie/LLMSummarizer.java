/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package projectmessagerie;

import java.util.List;

public class LLMSummarizer {

    private final HttpApiLLMBackend backend;

    public LLMSummarizer(HttpApiLLMBackend backend) {
        this.backend = backend;
    }

    public String summarize(List<String> messages) {

        if (messages.isEmpty()) return "";

        // On assemble toute la conversation dans un bloc
        StringBuilder fullConv = new StringBuilder();
        for (String msg : messages) {
            fullConv.append(msg).append("\n");
        }

        // prompt complet
        String prompt = """
        Tu reçois ci-dessous la transcription brute d'une conversation informelle (chat texte) entre plusieurs personnes.

        Ta tâche :
        - Faire un résumé en français, clair et concis.
        - Expliquer ce que chaque participant a exprimé (sans forcément tout détailler phrase par phrase).
        - Mettre en avant les idées principales, les questions posées et les réponses données.
        - Ne pas inventer de détails qui ne figurent pas dans la conversation.
        - Ne pas structurer comme un compte-rendu de réunion avec "décisions / actions / points bloquants",
          mais plutôt comme un résumé naturel de discussion.

        Conversation :
        """ + fullConv + """

        Résumé de la conversation :
        """;

        return backend.generateSummary(prompt);
    }
}

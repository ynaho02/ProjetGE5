/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.insa.ynaho01.javaFX;

/**
 *
 * @author marie
 */
import fr.insa.ynaho01.projetmessagerie.INetAdressUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import projectmessagerie.LLMSummarizer;
import static projectmessagerie.ServeurChat.diffuser;

//On veut faire un serveur multi-client capable de publier à tous les clients
//les msg postés par les autres 
public class ServeurChatInter {

    public static final int PORT = 50001;

    private static List<GestionClient> clientsConnectes
            = Collections.synchronizedList(new ArrayList<>());

    private static List<String> users
            = Collections.synchronizedList(new ArrayList<>());

    private static final List<String> historiqueMessages
            = Collections.synchronizedList(new ArrayList<>());

    //On crée une liste pour contenir l'ensemble des clients onnectés 
    public static class GestionClient extends Thread {

        private Socket connexion;
        private String NomClient;
        private PrintWriter sortie; // Pour envoyer des messages au client
        //Le printwriter permet d'écrire du texte facilement dans un flux
        //Tu fais un getOuput du socket et il faut le convertir en streamOuptut
        private BufferedReader entree; // Pour recevoir des messages du client

        public GestionClient(Socket connexion) {
            this.connexion = connexion;
            try {
                // Initialisation des flux d'entrée/sortie
                this.entree = new BufferedReader(
                        new InputStreamReader(connexion.getInputStream(), StandardCharsets.UTF_8)
                );
                this.sortie = new PrintWriter(
                        new OutputStreamWriter(connexion.getOutputStream(), StandardCharsets.UTF_8),
                        true // auto-flush activé
                );
            } catch (IOException ex) {
                System.out.println("Erreur initialisation client : " + ex.getMessage());
            }
        }

        @Override
        public void run() {

            try (BufferedReader entree = new BufferedReader(new InputStreamReader(this.connexion.getInputStream(), StandardCharsets.UTF_8))) {
                //On recupere ce qui a été écrit sur le serveur 
                System.out.println("Entrez votre nom");
                //On écrit le nom en premier et ensuite tant que qqch est écrit, 
                //on print ce qui a été écrit avec le niom du client
                this.NomClient = entree.readLine();

                if (this.NomClient == null || this.NomClient.trim().isEmpty()) {
                    this.NomClient = "Anonyme_";
                }
                synchronized (users) {
                    users.add(this.NomClient);
                }

                envoyerListeUtilisateurs();
                String message;
                while ((message = entree.readLine()) != null) {
                    synchronized (historiqueMessages) {
                       
                        System.out.println("reçu from : " + this.NomClient + " : " + message + "\n");
                        Timestamp quand = new Timestamp(System.currentTimeMillis());
                        String heure = "[" + quand.toString().substring(11, 19) + "]";

                        String messageComplet = heure + " " + "message reçu de " + this.NomClient + " : " + message + "\n";
                        System.out.println(messageComplet);
                        if (message.equals("FIN")) {
                            break;
                        }
                        diffuserMessage(message, this);
                        historiqueMessages.add(message);
                    }
                }
            } catch (IOException ex) {
                System.out.println("Déconnexion ou erreur I/O pour " + this.NomClient + " : " + ex.getMessage());
            } finally {

                Timestamp quand = new Timestamp(System.currentTimeMillis());
                String heure = "[" + quand.toString().substring(11, 19) + "]";
                String deco = heure + " " + this.NomClient + " " + " s'est déconnecté";
                synchronized (clientsConnectes) {
                    clientsConnectes.remove(this);
                }

                synchronized (users) {
                    users.remove(this.NomClient);
                }
                diffuserSystem(deco, this);
                envoyerListeUtilisateurs();

                try {
                    connexion.close();
                } catch (IOException ex) {
                    // ignore
                }

                System.out.println(deco);
            }

        }

        public void envoyerMessage(String message) {
            if (sortie != null) {
                sortie.println(message);
            }
        }

        public static void envoyerListeUtilisateurs() {
            String liste = "UTILISATEURS:" + String.join(",", users);
            for (GestionClient client : clientsConnectes) {
                client.envoyerMessage(liste);
            }
        }

        private static void diffuserSystem(String message, GestionClient emetteur) {
            synchronized (clientsConnectes) {
                for (GestionClient client : clientsConnectes) {
                    if (client != emetteur) {
                        client.envoyerMessage(message);
                    }
                }
            }
        }

        //méthode pour diffuser les messages 
        private static void diffuserMessage(String message, GestionClient emetteur) {
            Timestamp quand = new Timestamp(System.currentTimeMillis());
            String heure = "[" + quand.toString().substring(11, 19) + "]";

            String messageComplet = heure + " " + "message reçu de " + emetteur.getNomClient() + " : " + message;

            synchronized (clientsConnectes) {
                for (GestionClient client : clientsConnectes) {
                    if (client != emetteur) {
                        client.envoyerMessage(messageComplet);
                    }
                }
            }
        }
        
        private static void diffuserResume(String resume) { 
            synchronized (clientsConnectes) { 
                for (GestionClient client : clientsConnectes) { 
                    client.envoyerMessage("[SUMMARY] " + resume); 
                } 
            } 
        }

        public static void lancerThreadResume() {

    Thread t = new Thread(() -> {

        projectmessagerie.HttpApiLLMBackend backend =
            new projectmessagerie.HttpApiLLMBackend();

        LLMSummarizer summarizer = new LLMSummarizer(backend);

        int lastSize = 0;

        while (true) {
            try { Thread.sleep(50000); } catch (InterruptedException e) { return; }

            List<String> copy;
            synchronized (historiqueMessages) {
                copy = new ArrayList<>(historiqueMessages);
            }

            
            if (copy.size() == lastSize) continue;

            lastSize = copy.size();

            String resume = summarizer.summarize(copy);
            diffuserResume(resume);
            System.out.println("[Résumé LLM]: " + resume);
        }
    });

    t.setDaemon(true);
    t.start();
}
        public String getNomClient() {
            return this.NomClient;
        }
    }

    public List<String> getHistorique() {
        return this.historiqueMessages;
    }

    public static void multiClient() {

        try {
            Inet4Address host = INetAdressUtil.premiereAdresseNonLoopback();
            ServerSocket ss = new ServerSocket(PORT, 10, host);
            System.out.println("=================================");
            System.out.println("   SERVEUR DE CHAT DÉMARRÉ");
            System.out.println("=================================");
            System.out.println("IP   : " + host.getHostAddress());
            System.out.println("Port : " + PORT);
            System.out.println("En attente de connexions...\n");
            
            while (true) {
                Socket con = ss.accept();
                GestionClient GC = new GestionClient(con);
                GestionClient.lancerThreadResume();
                System.out.println("→ Nouvelle connexion depuis : "
                        + con.getInetAddress().getHostAddress());

                clientsConnectes.add(GC);
                GC.start();
            }

        } catch (IOException ex) {
            throw new Error(ex);
        }

    }

    public static void main(String[] args) {
        multiClient();
    }

}

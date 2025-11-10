/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.insa.ynaho01.projetmessagerie;

/**
 *
 * @author marie
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import projectmessagerie.ConsoleFdB;

public class ClientChat {
     // Thread qui lit les messages du serveur
    public static class LectureServeur extends Thread {

        private final Socket soc;

        public LectureServeur(Socket soc) {
            this.soc = soc;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(soc.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(">> " + line);
                }
            } catch (IOException ex) {
                System.out.println("Connexion au serveur terminée : " + ex.getMessage());
            }
        }
    }

    public static void clientChat() {
        try {
            String adr = ConsoleFdB.entreeString("adresse serveur : ");
            int port = 50001;
            System.out.println("Connexion au serveur " + adr + " sur le port " + port + " ...");
            
            Socket soc = new Socket(adr, port);
            System.out.println("Connecté au serveur :");
            System.out.println("  Adresse serveur : " + soc.getInetAddress().getHostAddress());
            System.out.println("  Port serveur    : " + soc.getPort());
            System.out.println("  Adresse locale  : " + soc.getLocalAddress().getHostAddress());
            System.out.println("  Port local      : " + soc.getLocalPort());

            String name = ConsoleFdB.entreeString("nom du client : ");

            try (PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(soc.getOutputStream(), StandardCharsets.UTF_8),
                    true // auto-flush
            )) {
                // envoyer le nom
                out.println(name);

                // démarrer le thread de lecture
                LectureServeur ls = new LectureServeur(soc);
                ls.start();

                System.out.println("Tapez vos messages. Écrivez FIN pour quitter.");

                String mess = "";
                while (!mess.equalsIgnoreCase("FIN")) {
                    mess = ConsoleFdB.entreeString("");
                    out.println(mess);
                }

            } finally {
                soc.close();
            }

        } catch (IOException ex) {
            throw new Error(ex);
        }
    }

    public static void main(String[] args) {
        clientChat();
    }

    
    
}

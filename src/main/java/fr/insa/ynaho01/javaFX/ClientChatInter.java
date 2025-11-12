/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.insa.ynaho01.javaFX;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import projectmessagerie.ConsoleFdB;

/**
 *
 * @author marie
 */
public class ClientChatInter {

    private Socket soc;
    private PrintWriter out;
    private final LinkedList<String> messagesRecus = new LinkedList<>();
    
    public LinkedList getMessagesRecus(){
        return this.messagesRecus;
    }

    public boolean connect(String username) {
        try {
            //String adr = ConsoleFdB.entreeString("adresse serveur : ");
            String adr = "10.172.26.40";
            int port = 50001;
            System.out.println("Connexion au serveur " + adr + " sur le port " + port + " ...");

            this.soc = new Socket(adr, port);
            System.out.println("Connecté au serveur :");
            System.out.println("  Adresse serveur : " + soc.getInetAddress().getHostAddress());
            System.out.println("  Port serveur    : " + soc.getPort());
            System.out.println("  Adresse locale  : " + soc.getLocalAddress().getHostAddress());
            System.out.println("  Port local      : " + soc.getLocalPort());
            
            this.out = new PrintWriter(
             new OutputStreamWriter(soc.getOutputStream(), StandardCharsets.UTF_8),
                true
            );
            
            out.println(username);

            LectureServeur ls = new LectureServeur(soc);
            ls.start();
            return true;

        } catch (IOException ex) {
            System.out.println("Erreur de connexion : " + ex.getMessage());
            return false;
        }

    }

    public String recupMessage(){
        synchronized (this.messagesRecus) {
            while(this.messagesRecus.isEmpty()){
                try {
                    this.messagesRecus.wait();
                } catch (InterruptedException e) {
                    return null;
                }
            }
            return this.messagesRecus.removeLast();
            
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public void close() throws IOException {
        if (soc != null) {
            soc.close();
        }
    }

    private class LectureServeur extends Thread {

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
                    synchronized (messagesRecus) {
                        messagesRecus.add(line);
                        messagesRecus.notify(); // réveille attendreMessage()
                    }
                    System.out.println(">> " + messagesRecus.getLast());
                }
                
            } catch (IOException ex) {
                System.out.println("Erreur lecture serveur: " + ex.getMessage());
            }
        }
    }
    



}



/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package messagerie;

/**
 *
 * @author hbenzbiria01
 */

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class client {

    public static final int PORT = 50001;

    public static void main(String[] args) {
        try {
            // 🖥️ Adresse du serveur
            InetAddress serveur = InetAddress.getLocalHost(); 
            // Si tu veux te connecter depuis une autre machine :
            // InetAddress serveur = InetAddress.getByName("192.168.x.x");

            Socket soc = new Socket(serveur, PORT);
            System.out.println("Connecté au serveur : " + serveur.getHostAddress() + ":" + PORT);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(soc.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(soc.getOutputStream(), true);
            Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8);

            // Thread pour recevoir les messages du serveur
            Thread lectureThread = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        System.out.println(msg);
                    }
                } catch (IOException e) {
                    System.out.println("Déconnecté du serveur.");
                }
            });
            lectureThread.start();

            // Envoi du pseudo
            System.out.print("Entrez votre pseudo : ");
            String pseudo = sc.nextLine();
            out.println(pseudo);

            // Boucle d'envoi des messages
            while (true) {
                String message = sc.nextLine();
                out.println(message);
            }

        } catch (IOException ex) {
            System.out.println("Erreur de connexion : " + ex.getMessage());
        }
    }
}



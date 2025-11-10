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
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

//On veut faire un serveur multi-client capable de publier à tous les clients
//les msg postés par les autres 

public class ServeurChat {
    
    public static final int PORT = 50001;
    
     private static List<GestionClient> clientsConnectes = 
            Collections.synchronizedList(new ArrayList<>());
        
     //On crée une liste pour contenir l'ensemble des clients onnectés 
    
    public static class GestionClient extends Thread {
        
        private Socket connexion;
        private String NomClient;
        private PrintWriter sortie; // Pour envoyer des messages au client
        //Le printwriter permet d'écrire du texte facilement dans un flux
        //Tu fais un getOuput du socket et il faut le convertir en streamOuptut
        private BufferedReader entree; // Pour recevoir des messages du client
        
        public GestionClient(Socket connexion){
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
        public void run(){
            
            try (BufferedReader entree = new BufferedReader(new InputStreamReader(this.connexion.getInputStream(), StandardCharsets.UTF_8))){
            //On recupere ce qui a été écrit sur le serveur 
            System.out.println("Entrez votre nom");
            //On écrit le nom en premier et ensuite tant que qqch est écrit, 
            //on print ce qui a été écrit avec le niom du client
            this.NomClient = entree.readLine();
            if (this.NomClient == null || this.NomClient.trim().isEmpty()) {
                    this.NomClient = "Anonyme_" + this.getId();
                }
            String message;
             while ((message = entree.readLine()) != null) {
                    System.out.println("reçu from : " + this.NomClient + " : " + message + "\n");
                    
                    if (message.equals("FIN")) {
                        break;
                    }
                    diffuserMessage(message, this);
                }
            
        }  catch (IOException ex) {
                throw new Error(ex);
            }
 
    }
    
   
    
    public void envoyerMessage(String nomExpediteur,String message){
        if (sortie != null) {
            sortie.println(nomExpediteur + " a dit : " + message);
            }
    }
    
    //méthode pour diffuser les messages 
    private static void diffuserMessage(String message, GestionClient emetteur) {
        synchronized(clientsConnectes){
            for (GestionClient client : clientsConnectes){
                if (client != emetteur){
                    client.envoyerMessage(emetteur.getNomClient(), message);
                }
            }
        }
    }
    
         public String getNomClient() {
            return this.NomClient;
        }
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
            
            while(true){
                Socket con = ss.accept();
                GestionClient GC = new GestionClient(con);
                System.out.println("→ Nouvelle connexion depuis : " + 
                                con.getInetAddress().getHostAddress());
                
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

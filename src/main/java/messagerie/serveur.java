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
import java.util.*;
import java.net.Inet4Address;
import java.net.InetAddress;



public class serveur {

    public static final int PORT = 50001;
    private static final List<Socket> clients = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        multiClient();
    }

    public static void multiClient() {
        try {
            Inet4Address host = (Inet4Address) InetAddress.getLocalHost();
            ServerSocket ss = new ServerSocket(PORT, 10, host);

            System.out.println("Serveur de chat en attente :");
            System.out.println("IP : " + host.getHostAddress());
            System.out.println("Port : " + PORT);

            while (true) {
                Socket soc = ss.accept();
                clients.add(soc);
                new GereClient(soc).start();
            }

        } catch (IOException ex) {
            throw new Error(ex);
        }
    }

    // Classe pour gérer chaque client
    public static class GereClient extends Thread {
        private String clientName;
        private Socket conn;

        public GereClient(Socket conn) {
            this.conn = conn;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(conn.getOutputStream(), true)
            ) {
                out.println("Entrez votre pseudo : ");
                this.clientName = in.readLine();
                broadcast(" " + clientName + " a rejoint la discussion.");

                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println("Message reçu de " + clientName + " : " + line);
                    broadcast(clientName + " : " + line);
                }

            } catch (IOException ex) {
                System.out.println(clientName + " s'est déconnecté.");
                broadcast(" " + clientName + " a quitté la discussion.");
            } finally {
                clients.remove(conn);
            }
        }

        // Envoie un message à tous les clients connectés
        private void broadcast(String message) {
            synchronized (clients) {
                for (Socket s : clients) {
                    try {
                        PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                        out.println(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }   
}


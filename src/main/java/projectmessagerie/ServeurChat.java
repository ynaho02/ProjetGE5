package projectmessagerie;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author ihssane
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
import java.util.List;



/**
 * Serveur de chat très simple : 
 * - chaque client envoie d'abord son nom
 * - tous les messages sont diffusés à tous les autres
 */
public class ServeurChat {

    // liste de tous les clients connectés
    private static final List<GereClient> clients =
            Collections.synchronizedList(new ArrayList<>());

    // méthode de diffusion à tous les clients
    public static void diffuser(String message, GereClient emetteur) {
        System.out.println("[DIFFUSION] " + message);
        synchronized (clients) {
            for (GereClient gc : clients) {
                // si tu veux que l’émetteur voie aussi son message, enlève ce if
                if (gc != emetteur) {
                    gc.envoyer(message);
                }
            }
        }
    }

    public static class GereClient extends Thread {

        private String clientName;
        private final Socket conn;
        private final PrintWriter out;

        public GereClient(Socket conn) throws IOException {
            this.conn = conn;
            this.out = new PrintWriter(
                    new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8),
                    true   // auto-flush
            );
        }

        public void envoyer(String message) {
            out.println(message);
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(this.conn.getInputStream(), StandardCharsets.UTF_8))) {

                // première ligne : le nom
                this.clientName = in.readLine();
                if (this.clientName == null) {
                    return;
                }

                System.out.println("Client connecté : " + this.clientName
                        + " (" + conn.getInetAddress().getHostAddress() + ":" + conn.getPort() + ")");

                // annoncer l'arrivée
                ServeurChat.diffuser("[serveur] " + this.clientName + " a rejoint le chat.", this);

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equalsIgnoreCase("FIN")) {
                        break;
                    }
                    System.out.println("reçu de " + this.clientName + " : " + line);
                    ServeurChat.diffuser(this.clientName + " : " + line, this);
                }

            } catch (IOException ex) {
                System.out.println("Erreur avec le client " + clientName + " : " + ex.getMessage());
            } finally {
                // déconnexion
                try {
                    conn.close();
                } catch (IOException ex) {
                    // ignore
                }
                synchronized (clients) {
                    clients.remove(this);
                }
                if (clientName != null) {
                    ServeurChat.diffuser("[serveur] " + clientName + " a quitté le chat.", this);
                }
                System.out.println("Client déconnecté : " + clientName);
            }
        }
    }

    public static void multiClient() {
        try {
            Inet4Address host = INetAdresseUtil.premiereAdresseNonLoopback();
            int port = ConsoleFdB.entreeInt("port du serveur (0 pour automatique) : ");
            ServerSocket ss = new ServerSocket(port, 10, host);
            System.out.println("Serveur de chat en attente :");
            System.out.println("ip   : " + host.getHostAddress());
            System.out.println("port : " + ss.getLocalPort()); // port réel si 0

            while (true) {
                Socket soc = ss.accept();
                System.out.println("Connexion acceptée :");
                System.out.println("  Adresse client : " + soc.getInetAddress().getHostAddress());
                System.out.println("  Port client    : " + soc.getPort());
                System.out.println("  Adresse locale : " + soc.getLocalAddress().getHostAddress());
                System.out.println("  Port local     : " + soc.getLocalPort());

                GereClient gc = new GereClient(soc);
                synchronized (clients) {
                    clients.add(gc);
                }
                gc.start();
            }
        } catch (IOException ex) {
            throw new Error(ex);
        }
    }

    public static void main(String[] args) {
        multiClient();
    }

}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package projectmessagerie;

/**
 *
 * @author ihssa
 */
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientChatGUI extends JFrame {

    private JTextArea chatArea;
    private JTextArea summaryArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton audioButton;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientChatGUI(String serverHost, int serverPort, String username) throws IOException {
        super("Chat - " + username);
        this.username = username;

        // --- Connexion au serveur ---
        this.socket = new Socket(serverHost, serverPort);
        this.out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),
                true
        );
        this.in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
        );

        // envoyer le nom en première ligne (comme ton client console)
        out.println(username);

        initUI();
        startListenerThread();
    }

    private void initUI() {
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        
        summaryArea = new JTextArea();
        summaryArea.setEditable(false);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);


        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(BorderFactory.createTitledBorder("Messages"));

        JScrollPane summaryScroll = new JScrollPane(summaryArea);
        summaryScroll.setBorder(BorderFactory.createTitledBorder("Résumé"));
    
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            chatScroll, summaryScroll);
        split.setResizeWeight(0.7);
        
        inputField = new JTextField();
        sendButton = new JButton("Envoyer");
        audioButton = new JButton(" Parler");

        sendButton.addActionListener(e -> sendTextMessage());
        inputField.addActionListener(e -> sendTextMessage());

        audioButton.addActionListener(e -> startAudioMessage());

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.add(inputField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(audioButton);
        buttonPanel.add(sendButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        getContentPane().setLayout(new BorderLayout(5, 5));
        getContentPane().add(split, BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Envoi de message texte tapé
    private void sendTextMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        out.println(text);                 // au serveur
        chatArea.append("Moi : " + text + "\n"); // affichage local
        inputField.setText("");
    }

    // Bouton audio : enregistre + transcrit + envoie
    private void startAudioMessage() {
        new Thread(() -> {
            try {
                SwingUtilities.invokeLater(() -> {
                    audioButton.setEnabled(false);
                    audioButton.setText("🎙 Enregistrement...");
                });

                // enregistre 5 secondes et transcrit
                String transcription = SpeechToText.recordAndTranscribe(5);

                if (transcription != null && !transcription.isBlank()) {
                    // on affiche et on envoie
                    chatArea.append("(audio) " + username + " : " + transcription + "\n");
                    out.println(transcription);
                } else {
                    chatArea.append("[audio] Rien reconnu.\n");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                chatArea.append("[audio] Erreur : " + ex.getMessage() + "\n");
            } finally {
                SwingUtilities.invokeLater(() -> {
                    audioButton.setEnabled(true);
                    audioButton.setText("🎙 Parler");
                });
            }
        }).start();
    }

    // Thread qui écoute en permanence les messages venant du serveur
    private void startListenerThread() {
        Thread t = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                     String msg = line;
                        SwingUtilities.invokeLater(() -> {
                            if (msg.startsWith("[SUMMARY]")) {
                                String resume = msg.substring("[SUMMARY]".length()).trim();
                                summaryArea.setText(resume + "\n"); 
                            } else {
                                chatArea.append(msg + "\n");
                            }
                        });
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() ->
                        chatArea.append("[System] Connexion au serveur perdue.\n"));
            } finally {
                try { socket.close(); } catch (IOException ignored) {}
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                String host = JOptionPane.showInputDialog(
                        null, "Adresse du serveur :", "192.168.56.1");
                if (host == null || host.isBlank()) return;

                String portStr = JOptionPane.showInputDialog(
                        null, "Port du serveur :", "5001");
                if (portStr == null || portStr.isBlank()) return;
                int port = Integer.parseInt(portStr.trim());

                String username = JOptionPane.showInputDialog(
                        null, "Votre pseudo :");
                if (username == null || username.isBlank()) return;

                new ClientChatGUI(host.trim(), port, username.trim());
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Erreur : " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}

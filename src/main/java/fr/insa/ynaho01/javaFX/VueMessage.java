/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.insa.ynaho01.javaFX;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import fr.insa.ynaho01.javaFX.VueConnexion;
import java.io.InputStream;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import fr.insa.ynaho01.projetmessagerie.Utils.utils;
import static fr.insa.ynaho01.projetmessagerie.Utils.utils.sleepAlea;
import static fr.insa.ynaho01.projetmessagerie.Utils.utils.sleepNoInterrupt;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author marie
 */
public class VueMessage extends BorderPane {

    private final ClientChatInter client;

    private VBox LeftPane;
    private VBox CenterPane;
    private HBox TopRightPane;

    private String username;
    private LinkedList<String> nouveaux;
    private SpeechToText sp;

    private final BorderPane main;
    private String lastResume = "";
    // Nouveau : Container pour les messages avec bulles
    private VBox messagesContainer;
    private ScrollPane messagesScrollPane;

    public VueMessage(String username, BorderPane main) {

        // Gradient rouge-bleu moderne (VOS couleurs)
        Stop[] stops = new Stop[]{
            new Stop(0, Color.web("#3a5eff")), // bleu néon
            new Stop(0.5, Color.web("#2c2c3c")), // fond sombre
            new Stop(1, Color.web("#e60039")) // rouge feu
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        this.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));

        this.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        this.main = main;
        this.username = username;
        this.client = new ClientChatInter();
        this.nouveaux = this.client.getMessagesRecus();
        boolean connected = client.connect(username);

        this.setPadding(new Insets(20));
        this.LeftPane = createLeftPane();
        this.CenterPane = createCenterPane();
        this.TopRightPane = createTopRightPane();

        this.sp = new SpeechToText();

        this.setCenter(this.CenterPane);
        this.setLeft(this.LeftPane);
        this.setTop(this.TopRightPane);
        
        startResumeWatcher();
    }

    private void startResumeWatcher() {
        Thread watcher = new Thread(() -> {
            while (true) {
                String r = client.getResume();
                if (r != null && !r.isEmpty() && !r.equals(lastResume)) {
                    lastResume = r;
                    Platform.runLater(() -> {
                        showPopUpClickable("📄 Nouveau résumé disponible — cliquer pour ouvrir", "#7e22ce");
                    });
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        watcher.setDaemon(true);
        watcher.start();
    }

    public VBox createLeftPane() {
        VBox userListPane = new VBox(10);
        userListPane.setPadding(new Insets(15));
        userListPane.setStyle(
                "-fx-background-color: rgba(44, 44, 60, 0.9);"
                + "-fx-text-fill: #f1f1f1;"
                + "-fx-background-radius: 20px;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 3);"
        );
        userListPane.setPrefWidth(220);
        userListPane.setMaxHeight(500);

        Label title = new Label("💬 Connectés");
        title.setStyle(
                "-fx-font-size: 16px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #2d3436;"
                + "-fx-padding: 5px 0 10px 0;"
        );

        VBox userContainer = new VBox(8);
        userContainer.setPadding(new Insets(10, 0, 0, 0));

        ScrollPane userScroll = new ScrollPane(userContainer);
        userScroll.setFitToWidth(true);
        userScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(userScroll, Priority.ALWAYS);

        refreshUser ru = new refreshUser(this.client, userContainer, this.client.getUtilisateursConnectes());
        ru.start();

        Button toggleMenu = new Button("☰");
        toggleMenu.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.9);"
                + "-fx-background-radius: 15px;"
                + "-fx-font-size: 20px;"
                + "-fx-text-fill: #2d3436;"
                + "-fx-padding: 10px 15px;"
                + "-fx-cursor: hand;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);"
        );
        toggleMenu.setOnMouseEntered(e -> toggleMenu.setStyle(
                toggleMenu.getStyle() + "-fx-background-color: white;"
        ));

        toggleMenu.setOnAction(e -> userListPane.setVisible(!userListPane.isVisible()));

        userListPane.getChildren().addAll(title, userScroll);

        VBox leftWrapper = new VBox(15, toggleMenu, userListPane);
        leftWrapper.setAlignment(Pos.TOP_LEFT);

        return leftWrapper;
    }

    public VBox createCenterPane() {
        VBox center = new VBox(20);
        center.setAlignment(Pos.TOP_CENTER);
        center.setPadding(new Insets(0, 20, 0, 20));
        VBox.setVgrow(center, Priority.ALWAYS);

        // Header avec titre
        Label title = new Label("Messages");
        title.setStyle(
                "-fx-font-size: 28px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: white;"
                + "-fx-padding: 10px 0 20px 0;"
        );

        // Zone des messages avec bulles - CORRECTION ICI
        messagesContainer = new VBox(15);
        messagesContainer.setPadding(new Insets(20));
        messagesContainer.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.98);"
        );
        messagesContainer.setMinHeight(400);

        messagesScrollPane = new ScrollPane(messagesContainer);
        messagesScrollPane.setFitToWidth(true);
        messagesScrollPane.setStyle(
                "-fx-background: rgba(255, 255, 255, 0.98);"
                + "-fx-background-color: rgba(255, 255, 255, 0.98);"
                + "-fx-border-color: transparent;"
                + "-fx-background-radius: 20px 20px 0 0;"
        );
        VBox.setVgrow(messagesScrollPane, Priority.ALWAYS);

        refreshMessage rm = new refreshMessage(this.client, messagesContainer, this.nouveaux);
        rm.start();

        // Barre d'envoi modernisée
        HBox sendBar = createSendBar();

        center.getChildren().addAll(title, messagesScrollPane, sendBar);

        return center;
    }

    private HBox createSendBar() {
        HBox sendBar = new HBox(12);
        sendBar.setAlignment(Pos.CENTER);
        sendBar.setPadding(new Insets(20));
        sendBar.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.98);"
                + "-fx-background-radius: 0 0 20px 20px;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, -2);"
        );

        TextField tfMessage = new TextField();
        tfMessage.setPromptText("Tapez votre message...");
        tfMessage.setPrefHeight(50);
        tfMessage.setStyle(
                "-fx-background-color: #f8f9fa;"
                + "-fx-background-radius: 25px;"
                + "-fx-padding: 0 20px;"
                + "-fx-border-color: #e9ecef;"
                + "-fx-border-radius: 25px;"
                + "-fx-border-width: 2px;"
                + "-fx-font-size: 14px;"
                + "-fx-text-fill: #2d3436;"
        );
        tfMessage.setOnMouseEntered(e -> tfMessage.setStyle(
                tfMessage.getStyle() + "-fx-border-color: #667eea;"
        ));
        HBox.setHgrow(tfMessage, Priority.ALWAYS);

        // Boutons avec icônes
        Button btnSend = createIconButton("images/send-message.png", "#667eea");
        Button btnMic = createIconButton("images/microphone_rouge.png", "#FF0844");
        Button btnStopMic = createIconButton("images/stop_recording.png", "#e74c3c");
        btnStopMic.setVisible(false);

        btnMic.setOnAction(e -> {
            try {
                this.sp.startRecording();
                btnMic.setDisable(true);
                btnStopMic.setVisible(true);
                showPopUp("🎤 Enregistrement en cours...", "#667eea");
            } catch (Exception ex) {
                showPopUp("❌ Erreur micro : " + ex.getMessage(), "#e74c3c");
            }
        });

        btnStopMic.setOnAction(e -> {
            String transcription = this.sp.stopRecording();
            btnMic.setDisable(false);
            btnStopMic.setVisible(false);

            if (!transcription.isEmpty()) {
                this.client.sendMessage(transcription);
                addMessageToUI(transcription, true);
                showPopUp("✅ Message vocal envoyé !", "#00b894");
            } else {
                showPopUp("⚠️ Aucun texte détecté", "#fdcb6e");
            }
        });

        btnSend.setOnAction(e -> {
            String msg = tfMessage.getText();
            if (!msg.isEmpty()) {
                this.client.sendMessage(msg);
                addMessageToUI(msg, true);
                tfMessage.clear();
                showPopUp("✅ Message envoyé !", "#00b894");
            }
        });

        sendBar.getChildren().addAll(tfMessage, btnSend, btnMic, btnStopMic);
        return sendBar;
    }

    private ImageView createIcon(String path) {
        ImageView Icon = new ImageView(getImage(path));
        Icon.setFitWidth(16);
        Icon.setFitHeight(16);
        Icon.setPreserveRatio(true);

        return Icon;
    }

    private Button createIconButton(String iconPath, String color) {
        ImageView icon = new ImageView(getImage(iconPath));
        icon.setFitWidth(24);
        icon.setFitHeight(24);
        icon.setPreserveRatio(true);

        Button btn = new Button();
        btn.setGraphic(icon);
        btn.setStyle(
                "-fx-background-color: " + color + ";"
                + "-fx-background-radius: 50%;"
                + "-fx-padding: 12px;"
                + "-fx-cursor: hand;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
                btn.getStyle() + "-fx-scale-x: 1.05; -fx-scale-y: 1.05;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                btn.getStyle().replace("-fx-scale-x: 1.05; -fx-scale-y: 1.05;", "")
        ));

        return btn;
    }

    private void addMessageToUI(String message, boolean isSent) {
        Platform.runLater(() -> {
            HBox messageRow = new HBox(10);
            messageRow.setAlignment(isSent ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            messageRow.setPadding(new Insets(5, 0, 5, 0));

            VBox messageContent = new VBox(5);
            messageContent.setMaxWidth(450);

            // Nom de l'utilisateur
            Label sender = new Label(isSent ? "Vous" : "Utilisateur");
            sender.setStyle(
                    "-fx-font-size: 11px;"
                    + "-fx-font-weight: bold;"
                    + "-fx-text-fill: #ff1e56;"
            );

            // Bulle de message
            Label messageBubble = new Label(message);
            messageBubble.setWrapText(true);
            messageBubble.setMaxWidth(400);
            messageBubble.setPadding(new Insets(12, 16, 12, 16));

            if (isSent) {
                messageBubble.setStyle(
                        "-fx-background-color: #3a5eff;"
                        + "-fx-background-radius: 18px 18px 5px 18px;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-size: 14px;"
                );
            } else {
                messageBubble.setStyle(
                        "-fx-background-color: #e60039;"
                        + "-fx-background-radius: 18px 18px 18px 5px;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-size: 14px;"
                );
            }

            // Timestamp
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            Label timestamp = new Label(time);
            timestamp.setStyle(
                    "-fx-font-size: 10px;"
                    + "-fx-text-fill: #b2bec3;"
            );

            messageContent.getChildren().addAll(sender, messageBubble, timestamp);

            if (isSent) {
                messageContent.setAlignment(Pos.CENTER_RIGHT);
            }

            messageRow.getChildren().add(messageContent);
            messagesContainer.getChildren().add(messageRow);

            // Auto-scroll vers le bas
            Platform.runLater(() -> messagesScrollPane.setVvalue(1.0));
        });
    }

    public HBox createTopRightPane() {
        
        
        ImageView userIcon = new ImageView(getImage("images/user_2.png"));
        userIcon.setFitWidth(40);
        userIcon.setFitHeight(40);

        Button btnUser = new Button(this.username + "", userIcon);
        btnUser.setStyle(
                "-fx-background-color: #778899;"
                + "-fx-background-radius: 999px;"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
                + "-fx-padding: 10px 16px;"
                + "-fx-cursor: hand;"
        );

        ContextMenu userMenu = new ContextMenu();

        // VOS icônes personnalisées
        ImageView logoutIcon = createIcon("images/logout.png");
        ImageView data = createIcon("images/data.png");
        ImageView infoIcon = createIcon("images/info.png");

        MenuItem info = new MenuItem("Infos utilisateur", infoIcon);
        MenuItem deco = new MenuItem("Déconnexion", logoutIcon);
        MenuItem resume = new MenuItem("Résumé des conversations", data);

        deco.setOnAction(e -> {
            try {
                this.client.close();
                this.main.setCenter(new VueConnexion(this.main));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        resume.setOnAction((t) -> {
            this.main.setCenter(new VueResume(this.main, this.client,this.username,this));
        });
        userMenu.getItems().addAll(info, resume, deco);
        btnUser.setOnAction(e -> userMenu.show(btnUser, Side.BOTTOM, 0, 0));

        HBox topRight = new HBox(btnUser);
        topRight.setAlignment(Pos.TOP_RIGHT);
        topRight.setPadding(new Insets(10));

        return topRight;
    }

    public class refreshUser extends Thread {

        private ClientChatInter cci;
        private VBox VB;
        private LinkedList<String> noms;

        public refreshUser(ClientChatInter cci, VBox VB, LinkedList<String> noms) {
            this.cci = cci;
            this.VB = VB;
            this.noms = noms;
        }

        @Override
        public void run() {
            while (true) {
                synchronized (this.cci.getUtilisateursConnectes()) {
                    Platform.runLater(() -> {
                        this.VB.getChildren().clear();
                        for (String nom : this.cci.getUtilisateursConnectes()) {
                            HBox userRow = new HBox(12);
                            userRow.setAlignment(Pos.CENTER_LEFT);
                            userRow.setPadding(new Insets(10));
                            userRow.setStyle(
                                    "-fx-background-color: white;"
                                    + "-fx-background-radius: 12px;"
                                    + "-fx-cursor: hand;"
                                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);"
                            );

                            userRow.setOnMouseEntered(e -> userRow.setStyle(
                                    userRow.getStyle() + "-fx-background-color: #f8f9fa;"
                            ));

                            // Avatar coloré
                            Circle avatar = new Circle(18);
                            avatar.setFill(Color.web("#667eea"));

                            Label userName = new Label(nom);
                            userName.setStyle(
                                    "-fx-font-size: 13px;"
                                    + "-fx-font-weight: 500;"
                                    + "-fx-text-fill: #2d3436;"
                            );

                            userRow.getChildren().addAll(avatar, userName);
                            this.VB.getChildren().add(userRow);
                        }
                    });
                }
                sleepNoInterrupt(1000);
            }
        }
    }

    public class refreshMessage extends Thread {

        private ClientChatInter cci;
        private VBox messageContainer;
        private LinkedList<String> liste;

        public refreshMessage(ClientChatInter cci, VBox messageContainer, LinkedList<String> liste) {
            this.cci = cci;
            this.messageContainer = messageContainer;
            this.liste = liste;
        }

        @Override
        public void run() {
            while (true) {
                if (!liste.isEmpty()) {
                    String msg = liste.removeLast();
                    addMessageToUI(msg, false);
                }
                sleepNoInterrupt(500);
            }
        }
    }

    public void showPopUp(String message, String color) {
        Stage popup = new Stage();
        popup.initStyle(StageStyle.UNDECORATED);

        Label msg = new Label(message);
        msg.setStyle(
                "-fx-background-color: " + color + ";"
                + "-fx-text-fill: white;"
                + "-fx-padding: 15px 25px;"
                + "-fx-font-weight: bold;"
                + "-fx-font-size: 14px;"
                + "-fx-background-radius: 10px;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);"
        );

        StackPane pane = new StackPane(msg);
        pane.setPadding(new Insets(10));

        Scene scene = new Scene(pane);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        popup.setAlwaysOnTop(true);
        popup.show();

        PauseTransition delay = new PauseTransition(Duration.seconds(2.5));
        delay.setOnFinished(e -> popup.close());
        delay.play();
    }

    private void showPopUpClickable(String message, String color) {
        Stage popup = new Stage();
        popup.initStyle(StageStyle.UNDECORATED);

        Label msg = new Label(message);
        msg.setStyle(
                "-fx-background-color: " + color + ";"
                + "-fx-text-fill: white;"
                + "-fx-padding: 15px 25px;"
                + "-fx-font-weight: bold;"
                + "-fx-font-size: 14px;"
                + "-fx-background-radius: 10px;"
                + "-fx-cursor: hand;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);"
        );

        msg.setOnMouseClicked(e -> {
            popup.close();
            this.main.setCenter(new VueResume(this.main, this.client, this.username,this));
        });

        StackPane pane = new StackPane(msg);
        pane.setPadding(new Insets(10));

        Scene scene = new Scene(pane);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        popup.setAlwaysOnTop(true);
        popup.show();

        PauseTransition delay = new PauseTransition(Duration.seconds(4));
        delay.setOnFinished(e -> popup.close());
        delay.play();
    }

    private Image getImage(String resourcePath) {
        try {
            File file = new File("assets/" + resourcePath);
            return new Image(file.toURI().toString(), 0, 0, true, true);
        } catch (Exception e) {
            System.out.println("Image introuvable : " + resourcePath);
            return null;
        }
    }
}

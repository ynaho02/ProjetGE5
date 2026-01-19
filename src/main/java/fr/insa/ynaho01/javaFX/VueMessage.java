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
import javafx.stage.Popup;
import javafx.stage.Window;

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
    // Container pour les messages avec bulles
    private VBox messagesContainer;
    private ScrollPane messagesScrollPane;

    private Popup toastPopup;

    private boolean recording = false;
    private Label currentToast = null;

    //stack pane pour les popup
    public VueMessage(String username, BorderPane main, ClientChatInter cci) {

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
        this.client = cci;
        this.nouveaux = this.client.getMessagesRecus();
        //boolean connected = client.connect(username);

        this.setPadding(new Insets(20));
        this.LeftPane = createLeftPane();
        this.CenterPane = createCenterPane();
        this.TopRightPane = createTopRightPane();

        this.sp = new SpeechToText();

        this.setCenter(this.CenterPane);
        this.setLeft(this.LeftPane);
        this.setTop(this.TopRightPane);

        //on crée un stackpane contenu dans la vue qui reste au centre en permanence
        startResumeWatcher();

    }

    private void startResumeWatcher() {
        Thread watcher = new Thread(() -> {
            while (true) {
                String r = client.getResume();
                if (r != null && !r.isEmpty() && !r.equals(lastResume)) {

                    if (recording) {
                        continue;
                    }

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
        userListPane.getStyleClass().add("sidebar");
        userListPane.setPrefWidth(240);

        Label title = new Label("👥 Contacts");
        title.getStyleClass().add("sidebar-title");

        VBox userContainer = new VBox(8);

        ScrollPane userScroll = new ScrollPane(userContainer);
        userScroll.setFitToWidth(true);
        userScroll.getStyleClass().add("scroll-pane-transparent");
        userScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(userScroll, Priority.ALWAYS);

        refreshUser ru = new refreshUser(this.client, userContainer, this.client.getUtilisateursConnectes());
        ru.start();

        userListPane.getChildren().addAll(title, userScroll);

        VBox leftWrapper = new VBox(12, userListPane);
        leftWrapper.setAlignment(Pos.TOP_LEFT);
        leftWrapper.setPadding(new Insets(10, 10, 10, 0));

        return leftWrapper;
    }

    public VBox createCenterPane() {
        VBox center = new VBox(20);
        center.setAlignment(Pos.TOP_CENTER);
        center.setPadding(new Insets(0, 20, 0, 20));
        VBox.setVgrow(center, Priority.ALWAYS);

        messagesContainer = new VBox(15);
        messagesContainer.setPadding(new Insets(18));
        messagesContainer.getStyleClass().add("chat-card");

        messagesScrollPane = new ScrollPane(messagesContainer);
        messagesScrollPane.setFitToWidth(true);
        messagesScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        messagesScrollPane.getStyleClass().add("chat-scroll");
        VBox.setVgrow(messagesScrollPane, Priority.ALWAYS);

        refreshMessage rm = new refreshMessage(this.client, messagesContainer, this.nouveaux);
        rm.start();

        //Barre d'envoie sous forme de Hbox
        HBox sendBar = createSendBar();
        sendBar.getStyleClass().add("sendbar");
        center.getChildren().addAll(messagesScrollPane, sendBar);

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
                recording = true;
                this.sp.startRecording();

                btnMic.setDisable(true);
                btnStopMic.setVisible(true);
                showPopUp("🎤 Enregistrement en cours...", "#667eea");
            } catch (Exception ex) {
                recording = false;
                btnMic.setDisable(false);
                btnStopMic.setVisible(false);
                showPopUp("❌ Erreur micro : " + ex.getMessage(), "#e74c3c");
            }

        });

        btnStopMic.setOnAction(e -> {
            try {
                recording = false;

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

            } catch (Exception ex) {
                recording = false;
                btnMic.setDisable(false);
                btnStopMic.setVisible(false);
                showPopUp("❌ Erreur stop micro : " + ex.getMessage(), "#e74c3c");
            }
        });

        btnSend.setOnAction(e -> {
            if (recording) {
                return;
            }
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
            String senderName = isSent ? "Vous" : "Utilisateur";
            String body = message;
            //le booleen isSent permet de décider qui de Vous, ou utilisateur sera utilisé pour le label

            if (!isSent && message.startsWith("[SYSTEM]\n")) {
                addSystemMessageToUI(message.substring("[SYSTEM]\n".length()));
                return;
            }

            if (!isSent) {
                //donc si isSent est false, ça veut dire que c'est pas l'user connecté qui envoie 
                int cut = message.indexOf("\n");
                if (cut >= 0) {
                    senderName = message.substring(0, cut).trim();
                    body = message.substring(cut + 1);
                    //on recupère corps du msg + nom de la personne qui a envoyé
                }
            }

            HBox messageRow = new HBox(10);
            messageRow.setAlignment(isSent ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            messageRow.setPadding(new Insets(5, 0, 5, 0));

            VBox messageContent = new VBox(5);
            messageContent.setMaxWidth(450);

            Label sender = new Label(senderName);
            sender.setStyle(
                    "-fx-font-size: 11px;"
                    + "-fx-font-weight: bold;"
                    + "-fx-text-fill: #ff1e56;"
            );

            Label messageBubble = new Label(body);
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
            Platform.runLater(() -> messagesScrollPane.setVvalue(1.0));
        });
    }

    private void addSystemMessageToUI(String text) {

        //fonction spéciale pour les messages systemes
        Platform.runLater(() -> {
            HBox row = new HBox();
            row.setAlignment(Pos.CENTER);
            row.setPadding(new Insets(8, 0, 8, 0));

            Label bubble = new Label(text);
            bubble.setWrapText(true);
            bubble.setMaxWidth(520);
            bubble.setPadding(new Insets(8, 14, 8, 14));
            bubble.setStyle(
                    "-fx-background-color: rgba(0,0,0,0.12);"
                    + "-fx-background-radius: 14px;"
                    + "-fx-text-fill: #2d3436;"
                    + "-fx-font-size: 12px;"
                    + "-fx-font-weight: 700;"
            );

            row.getChildren().add(bubble);
            messagesContainer.getChildren().add(row);
            Platform.runLater(() -> messagesScrollPane.setVvalue(1.0));
        });
    }

    public HBox createTopRightPane() {

        Label pageTitle = new Label("Messages");
        pageTitle.getStyleClass().addAll("page-title");

        ImageView userIcon = new ImageView(getImage("images/user_2.png"));
        userIcon.setFitWidth(34);
        userIcon.setFitHeight(34);

        Button btnUser = new Button(this.username, userIcon);
        btnUser.setStyle(
                "-fx-background-color: rgba(255,255,255,0.22);"
                + "-fx-background-radius: 999px;"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: 800;"
                + "-fx-padding: 10px 14px;"
                + "-fx-cursor: hand;"
        );

        ContextMenu userMenu = new ContextMenu();
        MenuItem resume = new MenuItem("Résumé des conversations", createIcon("images/data.png"));
        MenuItem deco = new MenuItem("Déconnexion", createIcon("images/logout.png"));

        deco.setOnAction(e -> {
            try {
                this.client.close();
                this.main.setCenter(new VueConnexion(this.main));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        resume.setOnAction(e -> this.main.setCenter(new VueResume(this.main, this.client, this.username, this)));

        userMenu.getItems().addAll(resume, deco);
        btnUser.setOnAction(e -> userMenu.show(btnUser, Side.BOTTOM, 0, 0));

        Region spacerL = new Region();
        Region spacerR = new Region();
        HBox.setHgrow(spacerL, Priority.ALWAYS);
        HBox.setHgrow(spacerR, Priority.ALWAYS);

        HBox top = new HBox(12, spacerL, pageTitle, spacerR, btnUser);
        top.setAlignment(Pos.CENTER);
        top.setPadding(new Insets(10, 10, 10, 10));

        return top;
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
                            userRow.getStyleClass().add("user-row");

                            Circle avatar = new Circle(16);
                            avatar.setFill(Color.web("#667eea"));

                            Label userName = new Label(nom);
                            userName.getStyleClass().add("user-name");

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

        if (toastPopup != null && toastPopup.isShowing()) {
            toastPopup.hide();
        }
        if (this.getScene() == null) {
            return;
        }

        Label msg = new Label(message);
        msg.setStyle(
                "-fx-background-color: " + color + ";"
                + "-fx-text-fill: white;"
                + "-fx-padding: 12px 18px;"
                + "-fx-font-weight: 800;"
                + "-fx-font-size: 13px;"
                + "-fx-background-radius: 12px;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 14, 0.25, 0, 6);"
        );
        msg.setWrapText(true);
        msg.setMaxWidth(520);

        toastPopup = new Popup();
        toastPopup.setAutoFix(true);
        toastPopup.setAutoHide(true);
        toastPopup.getContent().add(msg);

        Window w = this.getScene().getWindow();

        // On affiche d'abord pour avoir les dimensions réelles du label
        toastPopup.show(w);

        // Position TOP-CENTER
        double x = w.getX() + (w.getWidth() - msg.getWidth()) / 2.0;
        double y = w.getY() + 18;

        toastPopup.setX(x);
        toastPopup.setY(y);

        PauseTransition delay = new PauseTransition(Duration.seconds(2.5));
        delay.setOnFinished(e -> toastPopup.hide());
        delay.play();
    }

    private void showPopUpClickable(String message, String color) {

        if (toastPopup != null && toastPopup.isShowing()) {
            toastPopup.hide();
        }
        if (this.getScene() == null) {
            return;
        }
        if (recording) {
            return;
        }
//on affiche pas de poppup de résumé si on fait un enregistrement

        Label msg = new Label(message);
        msg.setStyle(
                "-fx-background-color: " + color + ";"
                + "-fx-text-fill: white;"
                + "-fx-padding: 12px 18px;"
                + "-fx-font-weight: 800;"
                + "-fx-font-size: 13px;"
                + "-fx-background-radius: 12px;"
                + "-fx-cursor: hand;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 14, 0.25, 0, 6);"
        );
        msg.setWrapText(true);
        msg.setMaxWidth(520);

        msg.setOnMouseClicked(e -> {
            toastPopup.hide();
            this.main.setCenter(new VueResume(this.main, this.client, this.username, this));
        });

        toastPopup = new Popup();
        toastPopup.setAutoFix(true);
        toastPopup.setAutoHide(true);
        toastPopup.getContent().add(msg);

        Window w = this.getScene().getWindow();

        toastPopup.show(w);

        double x = w.getX() + (w.getWidth() - msg.getWidth()) / 2.0;
        double y = w.getY() + 18;

        toastPopup.setX(x);
        toastPopup.setY(y);

        PauseTransition delay = new PauseTransition(Duration.seconds(4));
        delay.setOnFinished(e -> toastPopup.hide());
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

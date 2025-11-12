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
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import fr.insa.ynaho01.javaFX.VueConnexion;
import java.io.InputStream;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Ellipse;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import fr.insa.ynaho01.projetmessagerie.Utils.utils;
import static fr.insa.ynaho01.projetmessagerie.Utils.utils.sleepAlea;
import static fr.insa.ynaho01.projetmessagerie.Utils.utils.sleepNoInterrupt;
import java.util.LinkedList;
       

/**
 *
 * @author marie
 */
public class VueMessage extends HBox {

    private final ClientChatInter client;

    private VBox LeftBar;
    private VBox RightPane;

    private HBox BottomBar;
    private VBox MessageArea;
    private String username;
    private LinkedList <String> nouveaux;

    public VueMessage(String username) {

        Image fond = getImage("images/background3.jpg");
        if (fond != null) {
            BackgroundImage bgImage = new BackgroundImage(
                    fond,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(100, 100, true, true, false, false)
            );
            this.setBackground(new Background(bgImage));
        }

        this.username = username;
        this.client = new ClientChatInter();
        this.nouveaux = this.client.getMessagesRecus();
        boolean connected = client.connect(username);
        this.setSpacing(20);
        this.setPadding(new Insets(30));

        this.LeftBar = createLeftBar();
        this.RightPane = createRightPane();

        this.getChildren().addAll(this.LeftBar, this.RightPane);

    }

    public VBox createLeftBar() {

        VBox top = new VBox(15);
        top.setAlignment(Pos.TOP_LEFT);
        top.setPadding(new Insets(20));
        top.setPrefWidth(200); // largeur fixe

        ImageView logo = new ImageView(getImage("images/reddot.png"));
        logo.setFitHeight(60);
        logo.setSmooth(true);
        logo.setPreserveRatio(true);
        logo.setOpacity(0.85);
        logo.setStyle("-fx-background-radius: 12px;");

        Button btnUser = new Button(username);
        Button btnHistory = new Button("Historique");
        Button btnLogout = new Button("Déconnexion");

        styleButton(btnUser);
        styleButton(btnHistory);
        styleButton(btnLogout);

        top.getChildren().addAll(logo, btnUser, btnHistory, btnLogout);
        return top;

    }

    public VBox createRightPane() {

        VBox right = new VBox(20);
        right.setAlignment(Pos.TOP_CENTER);
        right.setPadding(new Insets(20));
        //right.setPrefWidth(600); // largeur fixe ou bindable

        VBox messageArea = createMessageArea();
        HBox bottomBar = createBottomBar();

        right.getChildren().addAll(messageArea, bottomBar);
        return right;

    }

    public VBox createMessageArea() {

        VBox MA = new VBox(10);
        MA.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Messages reçus");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; ; -fx-text-fill: #444;");

        TextArea messageArea = new TextArea();
        messageArea.setPrefHeight(400);
        messageArea.setEditable(false);
        messageArea.setStyle("-fx-control-inner-background: #fefefe; -fx-text-fill: #333;");

        Button bRefresh = new Button("Rafraichir");
        styleButton(bRefresh);
        /*
        bRefresh.setOnAction((t) -> {
            String nvmsg = client.recupMessage();
            messageArea.appendText(nvmsg + "\n");
        });
        */
        refreshMessage rm = new refreshMessage(this.client,messageArea, this.nouveaux);
        rm.start();

        MA.getChildren().addAll(title, bRefresh, messageArea);

        return MA;
    }
    public class refreshMessage extends Thread {
        
        private ClientChatInter cci;
        private TextArea TA;
        private LinkedList <String> liste;
        
        public refreshMessage(ClientChatInter cci,TextArea TA,LinkedList <String> liste ){
            this.cci = cci;
            this.TA = TA;
            this.liste = liste;
            
        }
        
        @Override
        public void run(){
            while(true){
              
                if(!liste.isEmpty()){
                    Platform.runLater(() -> {
                        String msg = liste.removeLast();
                            this.TA.appendText(msg + "\n");
                        
                    });
                }
                sleepNoInterrupt(500); // rafraîchissement toutes les 0.5s
            }
        }
        
    }
    public HBox createBottomBar() {

        HBox Bottom = new HBox(10);
        Bottom.setAlignment(Pos.BOTTOM_CENTER);

        TextField tfMessage = new TextField();
        tfMessage.setPromptText("Tapez votre message...");
        tfMessage.setPrefWidth(700);
        tfMessage.setPrefHeight(70);
        //tfMessage.setStyle();
        Button btnSend = new Button();
        btnSend.setGraphic(new ImageView(getImage("images/send.png")));
        //styleButton(btnSend);
        btnSend.setStyle("-fx-background-radius: 12px;"
                + "-fx-padding: 8px 16px;"
        );

        Button btnMic = new Button();
        btnMic.setGraphic(new ImageView(getImage("images/microphone.png")));
        //styleButton(btnMic);
        btnMic.setStyle("-fx-background-radius: 12px;"
                + "-fx-padding: 8px 16px;"
        );

        btnSend.setOnAction((t) -> {
            String msg = tfMessage.getText();
            if (!msg.isEmpty()) {
                this.client.sendMessage(msg);
                tfMessage.clear();
                showPopUp("Message envoyé !");
            } else {
                showPopUp("Erreur dans l'envoi du message");
            }
        });

        Bottom.getChildren().addAll(tfMessage, btnSend, btnMic);
        return Bottom;
    }

    public void showPopUp(String message) {

        Stage popup = new Stage();
        popup.initStyle(StageStyle.UNDECORATED);
        //on crée une stage minimaliste sans les éléments habituels pour avoir une
        //vraie popup
        Label msg = new Label(message);
        msg.setStyle("-fx-background-color: #ff6f61; -fx-text-fill: white; -fx-padding: 10px; -fx-font-weight: bold;");
        StackPane pane = new StackPane(msg);
        //On met le msg à publier dans le stackpane
        //on met ce stackpane dans une nouvelle scene qui va aller dans la stage
        Scene scene = new Scene(pane);
        popup.setScene(scene);
        popup.setAlwaysOnTop(true);
        popup.show();

        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        //On set la durée de la transition à 2 secondes
        delay.setOnFinished(e -> popup.close());
        delay.play();
        //on lance le delay
    }

    private Image getImage(String resourcePath) {
        InputStream input = getClass().getResourceAsStream("/" + resourcePath);
        //System.out.println("Image trouvée ? " + (input != null));
        return input != null ? new Image(input, 0, 0, true, true) : null;
    }

    private void styleButton(Button b) {
        b.setStyle(
                "-fx-background-color: #ff6f61;"
                + // orange doux
                "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 20px;"
                + "-fx-padding: 8px 16px;"
                + "-fx-font-size: 14px;"
        );
        b.setOnMouseEntered(e -> b.setStyle(
                "-fx-background-color: #ffcc70;"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 20px;"
                + "-fx-padding: 8px 16px;"
                + "-fx-font-size: 14px;"
        ));

        b.setOnMouseExited(e -> b.setStyle(
                "-fx-background-color: #ff6f61;"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 20px;"
                + "-fx-padding: 8px 16px;"
                + "-fx-font-size: 14px;"
        ));
    }
}

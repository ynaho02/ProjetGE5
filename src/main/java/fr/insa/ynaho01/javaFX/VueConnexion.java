/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.insa.ynaho01.javaFX;

import fr.insa.ynaho01.projetmessagerie.UserManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import static java.util.Collections.addAll;
import java.util.LinkedList;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Hyperlink;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

/**
 *
 * @author marie
 */
public class VueConnexion extends StackPane {

    private VBox root;
    private BorderPane header;
    private HBox center;
    private VBox loginBox;
    private VBox registerBox;
    private final LinkedList<String> users = new LinkedList<>();
    private BorderPane main;

    public BorderPane createHeader() {

        BorderPane h = new BorderPane();
        h.setPadding(new Insets(10, 20, 10, 20));

        ImageView logo = new ImageView(getImage("images/reddot.png"));
        logo.setFitHeight(80);
        logo.setFitWidth(80);   // 
        logo.setPreserveRatio(true);
        logo.setSmooth(true);
        logo.setOpacity(0.85);
        logo.getStyleClass().addAll("logo");

        // Définir le chemin
        Path path = new Path();
        path.getElements().add(new MoveTo(-200, 0)); // départ hors écran
        path.getElements().add(new LineTo(0, 0));    // arrivée à sa place

        PathTransition move = new PathTransition(Duration.seconds(2), path, logo);
        move.setInterpolator(Interpolator.EASE_BOTH);

// Rotation
        RotateTransition rotate = new RotateTransition(Duration.seconds(2), logo);
        rotate.setByAngle(720); // deux tours complets

// Jouer en parallèle
        ParallelTransition animation = new ParallelTransition(move, rotate);
        animation.play();

        //Message de bienvenue sur le site reddot
        Label titre = new Label("Bienvenue sur reddot !");
        titre.getStyleClass().add("welcome-title");

        Button btnNouvelleFenetre = new Button("Ouvrir une autre session");
        btnNouvelleFenetre.getStyleClass().add("button");
        btnNouvelleFenetre.setOnAction(e -> MainFX.ouvrirNouvelleFenetre());

        HBox leftHeader = new HBox(30); // espace entre logo et titre
        leftHeader.setAlignment(Pos.CENTER_LEFT);
        leftHeader.getChildren().addAll(logo, titre);

        h.setLeft(leftHeader);
        h.setRight(btnNouvelleFenetre);

        h.setRight(btnNouvelleFenetre);

        BorderPane.setAlignment(btnNouvelleFenetre, Pos.CENTER_RIGHT);

        return h;
    }

    public VBox createLoginBox() {

        VBox lB = new VBox(15);

        lB.setAlignment(Pos.CENTER_LEFT);
        lB.getStyleClass().add("glass-box");
        Label loginTitle = new Label("🔐 Se connecter");
        loginTitle.getStyleClass().add("label-title");

        TextField tfLoginUser = new TextField();
        tfLoginUser.setPromptText("Nom d'utilisateur");
        tfLoginUser.getStyleClass().add("text-field");

        PasswordField pfLoginPass = new PasswordField();
        pfLoginPass.setPromptText("Mot de passe");
        pfLoginPass.getStyleClass().add("password-field");

        Label feedbackLogin = new Label();
        feedbackLogin.setTextFill(Color.RED);
        feedbackLogin.setStyle("-fx-font-size: 12px;");

        Button btnLogin = new Button("Connexion");
        btnLogin.getStyleClass().add("button");

        //Gestion du bouton login
        btnLogin.setOnAction((t) -> {

            try {
                UserManager um = new UserManager();
                boolean ok = um.authenticate(tfLoginUser.getText(), pfLoginPass.getText());
                if (ok) {
                    System.out.println("Connexion réussie !");
                    this.main.setCenter(new VueMessage(tfLoginUser.getText(), this.main));
                    feedbackLogin.setText("Connexion réussie !");
                    feedbackLogin.setTextFill(Color.GREEN);

                } else {
                    System.out.println("Identifiants incorretcs");
                    feedbackLogin.setText("Identifiants incorrects");
                    feedbackLogin.setTextFill(Color.RED);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                System.getLogger(VueConnexion.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }

        });

        Hyperlink forgotLink = new Hyperlink("Mot de passe oublié ?");
        forgotLink.setOnAction(e -> {
            System.out.println("Lien mot de passe oublié cliqué");
        });
        forgotLink.getStyleClass().add("forgot-link");

        lB.getChildren().addAll(loginTitle, tfLoginUser, pfLoginPass, btnLogin, feedbackLogin, forgotLink);

        return lB;

    }

    public VBox createRegisterBox() {

        VBox rB = new VBox(15);
        rB.getStyleClass().add("glass-box");
        rB.setAlignment(Pos.CENTER_LEFT);

        Label registerTitle = new Label("📝 Nouveau sur reddot ? Inscrivez-vous !");
        registerTitle.getStyleClass().add("label-title");

        TextField tfRegisterUser = new TextField();
        tfRegisterUser.setPromptText("Nom d'utilisateur");
        tfRegisterUser.getStyleClass().add("text-field");

        PasswordField pfRegisterPass = new PasswordField();
        pfRegisterPass.setPromptText("Mot de passe");
        PasswordField pfConfirmPass = new PasswordField();

        pfRegisterPass.getStyleClass().add("password-field");
        pfConfirmPass.getStyleClass().add("password-field");

        pfConfirmPass.setPromptText("Confirmez le mot de passe");
        Button btnRegister = new Button("Inscription");
        btnRegister.getStyleClass().add("button");

        //Gestion du bouton inscription
        btnRegister.setOnAction((t) -> {
            if (pfRegisterPass.getText().equals(pfConfirmPass.getText())) {
                try {
                    UserManager um = new UserManager();
                    boolean ok = um.saveUser(tfRegisterUser.getText(), pfConfirmPass.getText());

                    if (ok) {
                        System.out.println("Inscription reussie");
                        this.main.setCenter(new VueMessage(tfRegisterUser.getText(), this.main));
                    } else {
                        System.out.println("Probleme déjà incrit");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                System.out.println("Mdp pas concordants");
            }
        });

        rB.getChildren().addAll(registerTitle, tfRegisterUser, pfRegisterPass, pfConfirmPass, btnRegister);

        return rB;
    }

    public HBox createCenter(VBox lB, VBox rB) {
        HBox centre = new HBox(80);
        centre.setAlignment(Pos.CENTER);
        centre.getChildren().addAll(lB, rB);

        return centre;
    }

    public VueConnexion(BorderPane main) {

        this.main = main;
        //set du fond d'ecran
        Image fond = getImage("images/new_one.png");
        if (fond != null) {

            BackgroundImage bgImage = new BackgroundImage(
                    fond,
                    BackgroundRepeat.SPACE,
                    BackgroundRepeat.SPACE,
                    BackgroundPosition.CENTER,
                    BackgroundSize.DEFAULT
            //new BackgroundSize(100, 100, true, true, false, false)
            );
            this.setBackground(new Background(bgImage));
        }
        //----------------------------------------------
        //Mise en place du reste de l'écran

        this.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        this.root = new VBox(40);
        this.root.setAlignment(Pos.TOP_CENTER);
        this.root.setPadding(new Insets(40));

        this.header = createHeader();
        this.loginBox = createLoginBox();

        this.registerBox = createRegisterBox();

        //this.loginBox.getStyleClass().add("glass-box");
        this.center = createCenter(this.loginBox, this.registerBox);

        TranslateTransition slideLogin = new TranslateTransition(Duration.seconds(1), loginBox);
        slideLogin.setFromX(-300);
        slideLogin.setToX(0);

        TranslateTransition slideRegister = new TranslateTransition(Duration.seconds(1), registerBox);
        slideRegister.setFromX(300);
        slideRegister.setToX(0);

        ParallelTransition entrance = new ParallelTransition(slideLogin, slideRegister);
        entrance.play();

        FadeTransition ftHeader = new FadeTransition(Duration.seconds(1.5), header);
        ftHeader.setFromValue(0);
        ftHeader.setToValue(1);
        ftHeader.play();

        this.root.getChildren().addAll(this.header, this.center);

        this.getChildren().add(this.root);
    }

    //fonction qui permet de retrouver une image dans le dossier src/main en précisant le reste du chemin
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

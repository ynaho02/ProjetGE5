/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.insa.ynaho01.javaFX;

import fr.insa.ynaho01.projetmessagerie.UserManager;
import java.io.IOException;
import java.io.InputStream;
import static java.util.Collections.addAll;
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

/**
 *
 * @author marie
 */
public class VueConnexion extends StackPane {

    private VBox root;
    private HBox header;
    private HBox center;
    private VBox loginBox;
    private VBox registerBox;

    private BorderPane main;

    public HBox createHeader() {

        HBox h = new HBox(20);
        h.setAlignment(Pos.CENTER_LEFT);
        ImageView logo = new ImageView(getImage("images/reddot.png"));
        logo.setFitHeight(80);
        logo.setSmooth(true);
        logo.setPreserveRatio(true);
        logo.setOpacity(0.85);

        //Message de bienvenue sur le site reddot
        Label titre = new Label("Bienvenue sur reddot !");
        titre.setStyle(
                "-fx-font-size: 36px;"
                + // plus grand
                "-fx-text-fill: linear-gradient(to right, #ff6f61, #ffcc70);"
                + // dégradé orange
                "-fx-font-family: 'Segoe UI', sans-serif;"
                + // police moderne
                "-fx-font-weight: bold;"
        );

        Button btnNouvelleFenetre = new Button("Ouvrir une autre session");
        btnNouvelleFenetre.setOnAction(e -> MainFX.ouvrirNouvelleFenetre());

        h.getChildren().addAll(logo, titre, btnNouvelleFenetre); //ajout des elements au Vbox

        return h;
    }

    public VBox createLoginBox() {

        VBox lB = new VBox(15);
        lB.setAlignment(Pos.CENTER_LEFT);
        lB.setStyle(
                "-fx-background-color: rgba(255,255,255,0.5);"
                + // plus fondu
                "-fx-padding: 25px;"
                + "-fx-background-radius: 15px;"
                + "-fx-effect: dropshadow(gaussian, #999999, 10, 0.5, 0, 0);"
        );

        Label loginTitle = new Label("Se connecter");
        loginTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");

        TextField tfLoginUser = new TextField();
        tfLoginUser.setPromptText("Nom d'utilisateur");

        PasswordField pfLoginPass = new PasswordField();
        pfLoginPass.setPromptText("Mot de passe");

        Button btnLogin = new Button("Connexion");
        styleButton(btnLogin);
        //Gestion du bouton login
        btnLogin.setOnAction((t) -> {

            try {
                UserManager um = new UserManager();
                boolean ok = um.authenticate(tfLoginUser.getText(), pfLoginPass.getText());
                if (ok) {
                    System.out.println("Connexion réussié !");
                    this.main.setCenter(new VueMessage(tfLoginUser.getText()));
                } else {
                    System.out.println("Identifiants incorretcs");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                System.getLogger(VueConnexion.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }

        });

        Label forgotLabel = new Label("Mot de passe oublié ?");
        forgotLabel.setStyle("-fx-text-fill: #555;");
        Button btnForgot = new Button("Cliquez");
        styleButton(btnForgot);

        lB.getChildren().addAll(loginTitle, tfLoginUser, pfLoginPass, btnLogin, forgotLabel, btnForgot);

        return lB;

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

    public VBox createRegisterBox() {

        VBox rB = new VBox(15);
        rB.setAlignment(Pos.CENTER_LEFT);
        rB.setStyle(
                "-fx-background-color: rgba(255,255,255,0.5);"
                + // plus fondu
                "-fx-padding: 25px;"
                + "-fx-background-radius: 15px;"
                + "-fx-effect: dropshadow(gaussian, #999999, 10, 0.5, 0, 0);"
        );

        Label registerTitle = new Label("Nouveau sur reddot ? Inscrivez-vous !");
        registerTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");

        TextField tfRegisterUser = new TextField();
        tfRegisterUser.setPromptText("Nom d'utilisateur");

        PasswordField pfRegisterPass = new PasswordField();
        pfRegisterPass.setPromptText("Mot de passe");
        PasswordField pfConfirmPass = new PasswordField();

        pfConfirmPass.setPromptText("Confirmez le mot de passe");
        Button btnRegister = new Button("Inscription");
        styleButton(btnRegister);
        //Gestion du bouton inscription

        btnRegister.setOnAction((t) -> {
            if (pfRegisterPass.getText().equals(pfConfirmPass.getText())) {
                try {
                    UserManager um = new UserManager();
                    boolean ok = um.saveUser(tfRegisterUser.getText(), pfConfirmPass.getText());

                    if (ok) {
                        System.out.println("Inscription reussie");
                        this.main.setCenter(new VueMessage(tfRegisterUser.getText()));
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
        //----------------------------------------------
        //Mise en place du reste de l'écran

        this.root = new VBox(40);
        this.root.setAlignment(Pos.TOP_CENTER);
        this.root.setPadding(new Insets(40));

        this.header = createHeader();
        this.loginBox = createLoginBox();
        this.registerBox = createRegisterBox();

        this.center = createCenter(this.loginBox, this.registerBox);

        this.root.getChildren().addAll(this.header, this.center);

        this.getChildren().add(this.root);
    }

    //fonction qui permet de retrouver une image dans le dossier src/main en précisant le reste du chemin
    private Image getImage(String resourcePath) {
        InputStream input = getClass().getResourceAsStream("/" + resourcePath);
        //System.out.println("Image trouvée ? " + (input != null));
        return input != null ? new Image(input, 0, 0, true, true) : null;
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.insa.ynaho01.javaFX;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 *
 * @author marie
 */
public class MainFX extends Application {

    public static void ouvrirNouvelleFenetre() {
        //fonction pour ouvrir plusieurs fenetres de connexion
        Stage newStage = new Stage();
        BorderPane root = new BorderPane();
        VueConnexion vc = new VueConnexion(root);
        root.setCenter(vc);
        Scene s = new Scene(root);
        newStage.setScene(s);
        newStage.setTitle("Nouvelle session reddot");
        newStage.show();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Connexion");

        BorderPane main = new BorderPane();
        VueConnexion VC = new VueConnexion(main);
        main.setCenter(VC);

        Scene s = new Scene(main);
        primaryStage.setScene(s);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}

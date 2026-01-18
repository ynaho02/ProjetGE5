package fr.insa.ynaho01.javaFX;

import java.io.File;
import java.io.InputStream;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class VueResume extends BorderPane {

    private final ClientChatInter client;
    private final BorderPane main;
    private TextArea TA;
    private String username;
    private VueMessage vue_stock;
    
    public VueResume(BorderPane main, ClientChatInter client, String username, VueMessage vue_stock) {

        this.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #1e3c72, #2a5298, #7e22ce);"
        );
        this.setPadding(new Insets(30));

        this.main = main;
        this.vue_stock = vue_stock;
        this.client = client;
        this.username = username;
        // HEADER
        HBox header = createHeader();

        // TEXT AREA
        TA = createTextArea();

        // Layout principal
        VBox content = new VBox(20, header, TA);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);

        this.setCenter(content);
    }

    private HBox createHeader() {
        HBox hb = new HBox();
        hb.setSpacing(20);
        hb.setAlignment(Pos.CENTER_LEFT);

        Button retour = new Button("← Retour");
        retour.setStyle(
            "-fx-background-color: #ffffff22;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 8px 15px;" +
            "-fx-cursor: hand;"
        );

        retour.setOnAction(e -> {
    this.main.setCenter(this.vue_stock);
});


        Label titre = new Label("Résumé de vos conversations");
        titre.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;"
        );

        hb.getChildren().addAll(retour, titre);
        return hb;
    }

    private TextArea createTextArea() {
        TextArea ta = new TextArea();
        ta.setEditable(false);
        ta.setWrapText(true);
        ta.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-control-inner-background: #ffffffdd;" +
            "-fx-text-fill: #2d3436;" +
            "-fx-background-radius: 12px;"
        );

        // Thread de mise à jour continue
        Thread watcher = new Thread(() -> {
            String last = "";
            while (true) {
                String r = client.getResume();
                if (r != null && !r.isEmpty() && !r.equals(last)) {
                    last = r;
                    Platform.runLater(() -> ta.setText(r));
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        watcher.setDaemon(true);
        watcher.start();

        return ta;
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

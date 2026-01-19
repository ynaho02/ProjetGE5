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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class VueResume extends BorderPane {

    private final ClientChatInter client;
    private final BorderPane main;
    private TextArea TA;
    private String username;
    private VueMessage vue_stock;

    public VueResume(BorderPane main, ClientChatInter client, String username, VueMessage vue_stock) {

        this.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        this.getStyleClass().add("resume-root");

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
        VBox card = new VBox(12, TA);
        card.getStyleClass().add("resume-card");
        card.setPadding(new Insets(18));
        card.setMaxWidth(1100);
        VBox.setVgrow(card, Priority.ALWAYS);

        VBox content = new VBox(18, header, card);
        content.setPadding(new Insets(10));
        content.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(card, Priority.ALWAYS);

        this.setCenter(content);

        this.setCenter(content);
    }

    private void copyToClipboard() {
        //Fonction pour copier du contenu grace au clipboard
        ClipboardContent content = new ClipboardContent();
        content.putString(TA.getText() == null ? "" : TA.getText());
        Clipboard.getSystemClipboard().setContent(content);
    }

    private HBox createHeader() {
        HBox hb = new HBox(16);
        hb.setAlignment(Pos.CENTER_LEFT);
        hb.getStyleClass().add("resume-header");

        Button retour = new Button("← Retour");
        retour.getStyleClass().add("btn-ghost");

        retour.setOnAction(e -> this.main.setCenter(this.vue_stock));

        VBox titles = new VBox(2);
        Label titre = new Label("Résumé de vos conversations");
        titre.getStyleClass().add("resume-title");

        Label sub = new Label("Compte : " + username);
        sub.getStyleClass().add("resume-subtitle");

        titles.getChildren().addAll(titre, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnCopy = new Button("⧉ Copier");
        btnCopy.getStyleClass().add("btn-primary");
        btnCopy.setOnAction(e -> copyToClipboard());

        hb.getChildren().addAll(retour, titles, spacer, btnCopy);
        return hb;
    }

    private TextArea createTextArea() {
        TextArea ta = new TextArea();
        ta.setEditable(false);
        ta.setWrapText(true);

        ta.setPromptText("Aucun résumé pour le moment…");
        ta.getStyleClass().add("resume-textarea");
        VBox.setVgrow(ta, Priority.ALWAYS);

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

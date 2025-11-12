/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.insa.ynaho01.javaFX;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 *
 * @author marie
 */
public class BoiteACoucou extends VBox{
    
    public TextField tfNom;
    public TextArea taMessage;
    public Button bCoucou;
    public Button bSalut;
    public Button bAutoSend;
    
    public BoiteACoucou(){
        this.tfNom = new TextField();
        HBox hbNom = new HBox(new Label("nom:"), this.tfNom);
        this.taMessage = new TextArea();
        this.bCoucou = new Button("Coucou");
        this.bCoucou.setOnMouseClicked((t) -> {
             this.addMessage("coucou "+this.tfNom.getText());
        });
        this.bSalut = new Button("Salut");
        this.bSalut.setOnAction((t) -> {
            this.addMessage("salut "+this.tfNom.getText());
        });
        HBox hbArea = new HBox(this.taMessage);
        HBox hbBoutons = new HBox(this.bCoucou,this.bSalut);
        this.getChildren().add(hbNom);
        this.getChildren().add(hbArea);
        this.getChildren().add(hbBoutons);
        //this.setTop(hbNom);
    }
    
    public void addMessage(String string){
         this.taMessage.appendText(string+"\n"); //met un string dans la area
    }
}

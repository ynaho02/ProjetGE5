/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.insa.ynaho01.projetmessagerie;

/**
 *
 * @author marie
 */
import org.json.*;
import java.io.*;
import java.nio.file.*;

public class UserManager {

    private static final String FILE = "users.json"; //On crée un fichier qui va stocker
    //l'ensemble des utilisateurs 
    private JSONArray users;

    public UserManager() throws IOException {
        users = loadUsers();
    }

    public JSONArray loadUsers() throws IOException {
        //Chargement du fichier Json et de son contenu tant qu'on connet le nom
        try {
            if (!Files.exists(Paths.get(FILE))) {
                return new JSONArray();
            }
            String content = new String(Files.readAllBytes(Paths.get(FILE))); //transformation du nom du fichier en path
            return new JSONArray(content);
            
            //retourne dans tous les cas un tableau d'utilisateurs
        } catch (IOException e) {
            return new JSONArray();
        }
    }

    public boolean saveUser(String username, String password) throws IOException {
        //ajout d'un utilisateur et de son mdp
        JSONArray users = loadUsers(); //on charge le tableau de contenu du fichier JSON
        JSONObject newUser = new JSONObject(); //on crée un nouvel objet à ajouter à ce tableau
        
        if(userExist(username)){
            return false;
        }
        newUser.put("username", username);
        newUser.put("password", password);
        users.put(newUser);

        Files.write(Paths.get(FILE), users.toString(4).getBytes());
        //users.toString(4) permet de formater avec un espace les données du fichier JSON

        //On sauvegarde les utilisateurs et leurs mdp dans le fichier JSON
        return true;
    }

    public boolean authenticate(String username, String password) throws IOException {
        JSONArray users = loadUsers();
        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);
            if (user.getString("username").equals(username) && user.getString("password").equals(password)) {
                return true;
            }
        }

        return false;
    }
    
    public boolean userExist(String username) throws IOException{
        //un user veut s'inscrre est ce qu'il est déjà dans le tableau ?
        JSONArray users = loadUsers();
        //charger le tableau et défiler dans le tableau
        //si y'a aucune correspondance avec le username entré on retourne false
        //sinon on retourne true
        for(int i = 0; i < users.length();i++){
            JSONObject user = users.getJSONObject(i);
            if(user.getString("username").equals(username)){
                return true;
            }
            
        }
        return false;
    }
    
}

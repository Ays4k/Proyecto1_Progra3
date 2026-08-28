package SRR.Singleton;

import java.util.Deque;
import java.util.ArrayDeque;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;



public class MasterStageController {

    //El stage donde van a ir cambiando las escenas
    private Stage primaryStage;
    //Singleton, solo existe un controlador de escenas
    private static MasterStageController instance;
    //Pila de escenas para poder volver a la anterior
    private Deque<Scene> sceneStack;

    private MasterStageController(Stage primaryStage) {
        //Constructor privado para evitar instanciación externa
        this.sceneStack = new ArrayDeque<>();
        this.primaryStage = primaryStage;
    }

    public static MasterStageController getInstance(Stage primaryStage) throws IllegalStateException {
        if (instance == null) {
            //Si no existe una instancia, se crea una nueva con el Stage proporcionado
            //(ciclo de vida de la aplicación)
            //Si ya existe una instancia, se ignora el Stage proporcionado y se devuelve la instancia existente
            instance = new MasterStageController(primaryStage);
        }
        else{
            throw new IllegalStateException("MasterStageController ya ha sido inicializado. No se puede crear otra instancia.");
        }
        return instance;
    }
    public static MasterStageController getInstance() throws IllegalStateException{
        if (instance == null) {
            throw new IllegalStateException("MasterStageController no ha sido inicializado. Llama a getInstance(Stage primaryStage) primero.");
        }
        return instance;
    }
    //Si se quiere cambiar de escena, se guarda la actual en la pila (si hay) y se cambia a la nueva
    public void cambiarEscena(String archivoFxml) {
        Scene nuevaEscena;
        try {
            Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource(archivoFxml));
            nuevaEscena = new Scene(root);
        } catch (Exception e) {
            throw new IllegalStateException("Error al cargar el archivo FXML: " + archivoFxml, e);
        }
        Scene currentScene = primaryStage.getScene();
        if (currentScene != null) {
            sceneStack.push(currentScene);
        }
        primaryStage.setScene(nuevaEscena);
        primaryStage.sizeToScene();
        if (!primaryStage.isShowing()) {
            primaryStage.show();
        }
    }
        //Si se quiere volver a la escena anterior, se saca de la pila y se cambia a ella.
    // Si no hay escenas en la pila, se cierra la aplicación
    public void volverEscenaAnterior() {
        if (!sceneStack.isEmpty()) {
            Scene previousScene = sceneStack.pop();
            primaryStage.setScene(previousScene);
        }
        else{
            Platform.exit();
        }
    }
}

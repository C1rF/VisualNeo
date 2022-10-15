package hkust.edu.visualneo.utils.frontend;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.io.IOException;
import static javafx.animation.Animation.INDEFINITE;


public class LoadAnimation {

    private Stage dialogStage;
    private SequentialTransition whole_animation;
    private final int NUM_LABELS = 3;

    public LoadAnimation(Stage primaryStage){

        dialogStage = new Stage();

        // Set the parent-child relationship
        dialogStage.initOwner(primaryStage);
        dialogStage.initStyle(StageStyle.UNDECORATED);
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        Pane pane = new Pane();
        Scene scene = new Scene(pane, primaryStage.getWidth(), primaryStage.getHeight());
        pane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.1);");
        scene.setFill(null);
        dialogStage.setScene(scene);

        String[] labels_content = {"Loading Database .", "Loading Database ..", "Loading Database ..."};
        Label[] labels = new Label[NUM_LABELS];
        FadeTransition[] fade_all = new FadeTransition[NUM_LABELS];
        FadeTransition[] show_all = new FadeTransition[NUM_LABELS];
        TranslateTransition[] delays = new TranslateTransition[NUM_LABELS];

        for(int i=0; i<NUM_LABELS; i++){
            // Set the labels
            labels[i] = new Label(labels_content[i]);
            labels[i].setFont(Font.font("Courier", 20));
            labels[i].setTextFill(Color.BLUE);
            labels[i].layoutXProperty().bind(pane.widthProperty().subtract(labels[i].widthProperty()).divide(2));
            labels[i].layoutYProperty().bind(pane.heightProperty().subtract(labels[i].heightProperty()).divide(2));
            pane.getChildren().add(labels[i]);
            // Set the animation
            fade_all[i] = new FadeTransition(Duration.millis(1),labels[i]);
            show_all[i] = new FadeTransition(Duration.millis(1),labels[i]);
            delays[i] = new TranslateTransition(Duration.millis(999));
            //setting the initial and the target opacity value for the transition
            fade_all[i].setFromValue(1);fade_all[i].setToValue(0);
            show_all[i].setFromValue(0);show_all[i].setToValue(1);
        }
        whole_animation = new SequentialTransition (
                delays[0],fade_all[0],show_all[1],
                delays[1],fade_all[1],show_all[2],
                delays[2],fade_all[2],show_all[0]
        );
        whole_animation.setCycleCount(INDEFINITE);
    }

    public void activateLoadAnimation() {
        System.out.println("Start the animation");
        dialogStage.show();
        whole_animation.play();
    }

    public Stage getDialogStage(){
        return dialogStage;
    }

    public void cancelLoadAnimation() {
        System.out.println("Stop the animation");
        dialogStage.close();
        whole_animation.stop();
    }
}
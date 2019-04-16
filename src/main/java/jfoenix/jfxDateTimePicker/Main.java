package jfoenix.jfxDateTimePicker;

import java.util.Locale;

import com.jfoenix.controls.JFXDateTimePicker;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Builder;

public class Main extends Application implements Builder<Node> {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        double width = screen.getWidth() / 4d * 2d;
        double height = screen.getHeight() / 4d * 2d;

        stage.setWidth(width);
        stage.setHeight(height);
        stage.setX((screen.getWidth() - width) / 2);
        stage.setY((screen.getHeight() - height) / 2);

        Locale.setDefault(new Locale("en"));

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        
        JFXDateTimePicker jfxDateTimePicker = new JFXDateTimePicker();
        jfxDateTimePicker.set24HourView(true);
        jfxDateTimePicker.setShowWeekNumbers(true);

        gridPane.add(jfxDateTimePicker, 0, 0);

        Scene scene = new Scene(gridPane);
        stage.setScene(scene);

        stage.setTitle("JFXDateTimePicker Demo");
        stage.show();
    }

    @Override
    public Node build() {
        // TODO Auto-generated method stub
        return null;
    }
}

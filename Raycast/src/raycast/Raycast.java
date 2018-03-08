package raycast;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Raycast extends Application {

    private Diagram diagram = new Diagram(704, 480);

    @Override
    public void start(Stage primaryStage) {

        HBox root = new HBox();
        Scene scene = new Scene(root);

        VBox menu = new VBox();

        //Move to origin
        Button centerBTN = new Button("Move to Center");
        centerBTN.setOnAction(i -> {
            diagram.viewportToCenter();
        });

        //Add new point to diagram
        Label pointlab = new Label("Add new point");
        GridPane.setHalignment(pointlab, HPos.CENTER);

        TextField xinput = new TextField();
        xinput.setPromptText("x");
        TextField yinput = new TextField();
        yinput.setPromptText("y");

        Button newPointBTN = new Button("Add new");
        newPointBTN.setOnAction(i -> {
            diagram.addNewPoint(
                    Double.parseDouble(xinput.getText()),
                    Double.parseDouble(yinput.getText())
            );
        });

        GridPane addPointPanel = new GridPane();
        addPointPanel.add(pointlab, 0, 0, 2, 1);
        addPointPanel.add(xinput, 0, 1);
        addPointPanel.add(yinput, 1, 1);
        addPointPanel.add(newPointBTN, 0, 2, 2, 1);

        addPointPanel.setHgap(5);
        addPointPanel.setVgap(5);
        addPointPanel.setPadding(new Insets(5, 5, 5, 5));
        addPointPanel.setStyle("-fx-border-color: derive(-fx-base, -20%); -fx-border-radius: 2px;");

        //Button newPointBTN = new Button("Add new");
        newPointBTN.setOnAction(e -> {
            diagram.addNewPoint(
                    Double.parseDouble(xinput.getText()),
                    Double.parseDouble(yinput.getText())
            );
        });

        centerBTN.setPrefWidth(Integer.MAX_VALUE);
        newPointBTN.setPrefWidth(Integer.MAX_VALUE);

        menu.setPrefWidth(120);
        menu.setSpacing(10);
        root.setSpacing(10);
        root.setPadding(new Insets(10, 10, 10, 10));

        root.getChildren().addAll(
                menu,
                diagram
        );

        menu.getChildren().addAll(
                centerBTN,
                addPointPanel
        );

        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
        primaryStage.setTitle("Raycast");
        primaryStage.setScene(scene);
        primaryStage.show();

        //Starts everything
        diagram.run();
    }

    public static void main(String[] args) {
        launch(args);
    }

}

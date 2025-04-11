package oop.tegevusteplaneerija.client;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import oop.tegevusteplaneerija.common.CalendarEvent;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import oop.tegevusteplaneerija.common.CalendarEvent;
import oop.tegevusteplaneerija.common.DatabaseManager;

import java.sql.SQLException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainClient extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private static DatabaseManager startDatabase() throws SQLException {
        DatabaseManager dbm = new DatabaseManager("client.db");
        dbm.init();
        dbm.insertEvent("Kliendi sündmus", "lihtsalt prooviks");
        return dbm;
    }

    @Override
    public void start(Stage primaryStage) throws SQLException {
        // Create an event using the common module
        CalendarEvent event1 = new CalendarEvent(
                "Client Meeting",
                "Discuss UI design",
                "2:00 PM",
                "3:00 PM"
        );
        CalendarEvent event2 = new CalendarEvent(
                "foo",
                "bar",
                "4:00 PM",
                "5:00 PM"
        );

        List<CalendarEvent> event = List.of(event1, event2);

        VBox events = new VBox();
        BorderPane root = new BorderPane(events);


        event.stream().forEach(e -> addEvent(e, events));

        ContextMenu contextMenu = new ContextMenu();
        MenuItem newEventItem = new MenuItem("Add Event");
        newEventItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    var dialog = new EventDialog(actionEvent, primaryStage);
                    var event = dialog.waitForResult();
                    if (event != null) addEvent(event, events);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        contextMenu.getItems().add(newEventItem);

        root.setOnContextMenuRequested(e -> {
            contextMenu.show(root.getScene().getWindow(), e.getScreenX(), e.getScreenY());
        });

        Scene scene = new Scene(root, 300, 200);

        DatabaseManager dbm = startDatabase();
        dbm.printAll();
        primaryStage.setTitle("Calendar Client");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static void addEvent(CalendarEvent e, VBox events) {
        EventWidgetController c = new EventWidgetController(e);
        events.getChildren().add(c);
    }
}

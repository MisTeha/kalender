package oop.tegevusteplaneerija.client;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    public void start(Stage primaryStage) throws SQLException, IOException {
        // Create an event using the common module
        CalendarEvent event1 = new CalendarEvent(
                "Client Meeting",
                "Discuss UI design",
                ZonedDateTime.parse("2025-04-01T13:00:00+02:00"),
                ZonedDateTime.parse("2025-04-01T14:00:00+02:00")
        );
        CalendarEvent event2 = new CalendarEvent(
                "foo",
                "bar",
                ZonedDateTime.parse("2025-04-02T13:00:00+02:00"),
                ZonedDateTime.parse("2025-04-02T14:00:00+02:00")
        );

        List<CalendarEvent> event = List.of(event1, event2);

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("EventsView.fxml"));
        Pane eventsView = loader.load();
        EventViewsController cont = loader.getController();
        Scene scene = new Scene(eventsView);

        event.stream().forEach(e -> cont.addEvent(e));

        DatabaseManager dbm = startDatabase();
        dbm.printAll();
        primaryStage.setTitle("Calendar Client");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

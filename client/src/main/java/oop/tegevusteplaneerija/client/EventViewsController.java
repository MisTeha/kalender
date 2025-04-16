package oop.tegevusteplaneerija.client;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import oop.tegevusteplaneerija.common.CalendarEvent;

import java.io.IOException;

public class EventViewsController {
    @FXML
    VBox events;

    @FXML
    BorderPane bPane;


    @FXML
    public void initialize() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem newEventItem = new MenuItem("Add Event");
        newEventItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    var dialog = new EventDialog(actionEvent, (Stage) bPane.getScene().getWindow());
                    var event = dialog.waitForResult();
                    if (event != null) addEvent(event);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        contextMenu.getItems().add(newEventItem);

        bPane.setOnContextMenuRequested(e -> {
            contextMenu.show(bPane.getScene().getWindow(), e.getScreenX(), e.getScreenY());
        });
    }

    public void addEvent(CalendarEvent e) {
        EventWidgetController c = new EventWidgetController(e, this);
        events.getChildren().add(c);
    }

    public void removeEvent(EventWidgetController c) {
        events.getChildren().remove(c);
    }
}

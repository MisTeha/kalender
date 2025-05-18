package oop.tegevusteplaneerija.client;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import oop.tegevusteplaneerija.common.CalendarEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatesContainerController {
    @FXML
    private VBox container;

    @FXML
    private BorderPane bPane;

    @FXML
    private void initialize() {

        ContextMenu contextMenu = new ContextMenu();
        MenuItem newEventItem = new MenuItem("Add Event");
        newEventItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    var dialog = new EventDialog(actionEvent, (Stage) container.getScene().getWindow());
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
        EventDateContainerController con = container.getChildren().stream().map(c -> (EventDateContainerController) c).filter(c -> c.getLdate().equals(e.getStartTime().toLocalDate()))
                .reduce((a, b) -> {
                    throw new RuntimeException("Kaks sama kuupäevaga EventDateContainerit!");
                }).orElse(null);

        if (con == null) {
            con = new EventDateContainerController();
            con.setParent(this);
            container.getChildren().add(con);
        }

        con.addEvent(e);
    }

    public void remove(EventDateContainerController c) {
        var ch = c.getChildren();
        container.getChildren().remove(c);
    }
}

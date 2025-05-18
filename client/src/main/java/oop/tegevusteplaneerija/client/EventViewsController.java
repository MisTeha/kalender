package oop.tegevusteplaneerija.client;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import oop.tegevusteplaneerija.common.CalendarEvent;

import java.io.IOException;

public class EventViewsController extends BorderPane {
    @FXML
    VBox events;


    EventDateContainerController parent;


    public EventViewsController() {
        FXMLLoader loader = new FXMLLoader(EventViewsController.class.getClassLoader().getResource("EventsView.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setParent(EventDateContainerController parent) {
        this.parent = parent;
    }

    public void addEvent(CalendarEvent e) {
        EventWidgetController c = new EventWidgetController(e, this);
        events.getChildren().add(c);
    }

    public void removeEvent(EventWidgetController c) {
        events.getChildren().remove(c);
        if (events.getChildren().isEmpty()) {
            parent.removeSelf();
        }
    }
}

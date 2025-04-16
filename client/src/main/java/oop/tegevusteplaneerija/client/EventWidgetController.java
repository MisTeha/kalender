package oop.tegevusteplaneerija.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import oop.tegevusteplaneerija.common.CalendarEvent;

import java.io.IOException;

public class EventWidgetController extends VBox {

    @FXML
    private Label titleLabel, descLabel;

    private EventViewsController parent;

    private CalendarEvent e;

    public EventWidgetController() {
        FXMLLoader loader = new FXMLLoader(EventWidgetController.class.getClassLoader().getResource("EventWidget.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public EventWidgetController(CalendarEvent e) {
        this();
        titleLabel.setText("%s: %s to %s".formatted(e.getTitle(), e.getStartTime(), e.getEndTime()));
        descLabel.setText(e.getDescription());
        this.e = e;
    }

    public EventWidgetController(CalendarEvent e, EventViewsController par) {
        this(e);
        parent = par;
    }

    @FXML
    void remove() {
        parent.removeEvent(this);
    }
}

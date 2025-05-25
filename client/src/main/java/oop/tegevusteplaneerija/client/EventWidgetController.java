package oop.tegevusteplaneerija.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import oop.tegevusteplaneerija.common.mudel.CalendarEvent;

import java.io.IOException;

public class EventWidgetController extends VBox {

    @FXML
    private Label titleLabel, descLabel, groupLabel;

    private EventViewsController parent;

    private CalendarEvent e;

    public EventWidgetController() {
        FXMLLoader loader = new FXMLLoader(
                EventWidgetController.class.getClassLoader().getResource("EventWidget.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static EventWidgetController create(CalendarEvent e, EventViewsController par) {
        EventWidgetController controller = new EventWidgetController();
        controller.setData(e, par);
        return controller;
    }

    private void setData(CalendarEvent e, EventViewsController par) {
        titleLabel.setText(
                "%s: %s to %s".formatted(e.getNimi(), e.getAlgushetk().toLocalTime(), e.getLopphetk().toLocalTime()));
        descLabel.setText(e.getKirjeldus());
        groupLabel.setText(e.getGrupp() != null ? e.getGrupp().getNimi() : "");
        this.e = e;
        this.parent = par;
    }

    @FXML
    public void remove() {
        parent.removeEvent(this);
    }

    public CalendarEvent getEvent() {
        return e;
    }
}

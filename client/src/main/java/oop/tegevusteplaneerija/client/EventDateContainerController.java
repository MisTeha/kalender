package oop.tegevusteplaneerija.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import oop.tegevusteplaneerija.common.CalendarEvent;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;

public class EventDateContainerController extends VBox {
    @FXML
    private Label date;

    @FXML
    private Pane container;

    private EventViewsController viewsController;

    private LocalDate ldate;

    private DatesContainerController parent;

    public EventDateContainerController() {
        FXMLLoader loader = new FXMLLoader(EventDateContainerController.class.getClassLoader().getResource("EventDateContainer.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        EventViewsController view = new EventViewsController();
        container.getChildren().add(view);
        viewsController = view;
        viewsController.setParent(this);
    }

    public void addEvent(CalendarEvent e) {
        viewsController.addEvent(e);
        setDate(e.getStartTime());
    }

    public void setDate(ZonedDateTime t) {
        ldate = t.toLocalDate();
        date.setText(ldate.toString());
    }

    public LocalDate getLdate() {
        return ldate;
    }

    public void setParent(DatesContainerController parent) {
        this.parent = parent;
    }

    public void removeSelf() {
        parent.remove(this);
    }
}

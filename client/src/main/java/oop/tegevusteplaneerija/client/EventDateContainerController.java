package oop.tegevusteplaneerija.client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import oop.tegevusteplaneerija.common.mudel.CalendarEvent;
import oop.tegevusteplaneerija.common.teenused.EventTeenus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EventDateContainerController extends VBox {
    @FXML
    private Label date;

    @FXML
    private Pane container;

    private EventViewsController viewsController;

    private LocalDate ldate;

    private List<CalendarEvent> events = new ArrayList<>();

    private DatesContainerController parent;

    private oop.tegevusteplaneerija.common.teenused.EventTeenus eventTeenus;

    private oop.tegevusteplaneerija.common.mudel.Kasutaja activeUser;

    public EventDateContainerController() {
        // liigutatud alumisse initialize funktsiooni
    }

    @FXML
    private void initialize() {
        viewsController = new EventViewsController();
        container.getChildren().add(viewsController);
        viewsController.setParent(this);
        if (eventTeenus != null)
            viewsController.setEventTeenus(eventTeenus);
        if (activeUser != null)
            viewsController.setActiveUser(activeUser);
    }

    public void addEvent(CalendarEvent e) {
        if (viewsController == null) {
            viewsController = new EventViewsController();
            viewsController.setParent(this);
            this.getChildren().add(viewsController);
            if (eventTeenus != null)
                viewsController.setEventTeenus(eventTeenus);
            if (activeUser != null)
                viewsController.setActiveUser(activeUser);
        }
        events.add(e);
        viewsController.addEvent(e);
        setDate(e.getAlgushetk().toLocalDate());
    }

    public void setDate(LocalDate ldate) {
        this.ldate = ldate;
        date.setText(ldate.toString());
    }

    public LocalDate getLdate() {
        return ldate;
    }

    public void setLdate(LocalDate ldate) {
        this.ldate = ldate;
    }

    public void setParent(DatesContainerController parent) {
        this.parent = parent;
    }

    public void removeSelf() {
        parent.remove(this);
    }

    public void setEventTeenus(EventTeenus eventTeenus) {
        this.eventTeenus = eventTeenus;
        if (viewsController != null) {
            viewsController.setEventTeenus(eventTeenus);
        }
    }

    public void setActiveUser(oop.tegevusteplaneerija.common.mudel.Kasutaja user) {
        this.activeUser = user;
        if (viewsController != null) {
            viewsController.setActiveUser(user);
        }
    }

    public DatesContainerController getParentController() {
        return parent;
    }
}

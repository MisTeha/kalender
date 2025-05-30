package oop.tegevusteplaneerija.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import oop.tegevusteplaneerija.common.mudel.CalendarEvent;

import java.io.IOException;

public class EventViewsController extends BorderPane {
    @FXML
    VBox events;

    EventDateContainerController parent;

    // --- Database integration ---
    private oop.tegevusteplaneerija.common.teenused.EventTeenus eventTeenus;
    private oop.tegevusteplaneerija.common.mudel.Kasutaja activeUser;

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

    public void setEventTeenus(oop.tegevusteplaneerija.common.teenused.EventTeenus eventTeenus) {
        this.eventTeenus = eventTeenus;
    }

    public void setActiveUser(oop.tegevusteplaneerija.common.mudel.Kasutaja user) {
        this.activeUser = user;
    }

    public void addEvent(CalendarEvent e) {
        // Only add to UI, do not save to DB here!
        EventWidgetController c = EventWidgetController.create(e, this);
        events.getChildren().add(c);
    }

    public void removeEvent(EventWidgetController c) {
        // Remove from DB if possible
        if (eventTeenus != null && activeUser != null) {
            try {
                var e = c.getEvent();
                int id = e.getId();
                var eventGroup = e.getGrupp();
                var modelEvent = new CalendarEvent(
                        id,
                        e.getNimi(),
                        e.getKirjeldus(),
                        e.getAlgushetk(),
                        e.getLopphetk(),
                        eventGroup);
                if (modelEvent.getId() > 0) {
                    eventTeenus.kustutaSündmus(modelEvent);
                } else {
                    // Fallback: search all groups the user belongs to
                    if (parent != null && parent.getParentController() != null) {
                        DatesContainerController datesParent = parent.getParentController();
                        if (datesParent.getGrupiTeenus() != null) {
                            java.util.List<oop.tegevusteplaneerija.common.mudel.Grupp> userGroups = datesParent
                                    .getGrupiTeenus().leiaKasutajaGrupid(activeUser.getId());
                            for (oop.tegevusteplaneerija.common.mudel.Grupp group : userGroups) {
                                java.util.List<CalendarEvent> all = eventTeenus.leiaGrupiSündmused(group.getId());
                                for (CalendarEvent ev : all) {
                                    if (ev.getNimi().equals(modelEvent.getNimi()) &&
                                            ev.getKirjeldus().equals(modelEvent.getKirjeldus()) &&
                                            ev.getAlgushetk().equals(modelEvent.getAlgushetk()) &&
                                            ev.getLopphetk().equals(modelEvent.getLopphetk())) {
                                        eventTeenus.kustutaSündmus(ev);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        events.getChildren().remove(c);
        if (events.getChildren().isEmpty()) {
            parent.removeSelf();
        }
    }
}

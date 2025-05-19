package oop.tegevusteplaneerija.client;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import oop.tegevusteplaneerija.common.mudel.CalendarEvent;
import oop.tegevusteplaneerija.common.mudel.Grupp;
import oop.tegevusteplaneerija.common.teenused.EventTeenus;
import oop.tegevusteplaneerija.common.teenused.GrupiTeenus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatesContainerController {
    @FXML
    private VBox container;

    @FXML
    private BorderPane bPane;

    private List<Grupp> allGroups = new ArrayList<>();
    private Grupp selectedGroup;
    private GrupiTeenus grupiTeenus;
    private EventTeenus eventTeenus;

    public void setGroupTeenus(GrupiTeenus grupiTeenus, int userId) {
        this.grupiTeenus = grupiTeenus;
        try {
            allGroups = grupiTeenus.leiaKasutajaGrupid(userId);
            if (!allGroups.isEmpty()) {
                selectedGroup = allGroups.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setEventTeenus(EventTeenus eventTeenus) {
        this.eventTeenus = eventTeenus;
    }

    public void setSelectedGroup(Grupp group) {
        this.selectedGroup = group;
    }

    @FXML
    private void initialize() {

        ContextMenu contextMenu = new ContextMenu();
        MenuItem newEventItem = new MenuItem("Add Event");
        newEventItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    var dialog = new EventDialog(actionEvent, (Stage) container.getScene().getWindow());
                    EventDialogController controller = dialog.getFxmlLoader().getController();
                    controller.setGrupp(selectedGroup);
                    var event = dialog.waitForResult();
                    if (event != null)
                        addEvent(event);
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
        // kas grupp on olemas ja korrektne
        if (eventTeenus != null && selectedGroup != null && (e.getId() < 0)) {
            try {
                var dbEvent = new CalendarEvent(e.getNimi(), e.getKirjeldus(), e.getAlgushetk(), e.getLopphetk(),
                        selectedGroup);
                eventTeenus.lisaSündmus(dbEvent);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        EventDateContainerController con = container.getChildren().stream().map(c -> (EventDateContainerController) c)
                .filter(c -> c.getLdate().equals(e.getAlgushetk().toLocalDate())).reduce((a, b) -> {
                    throw new RuntimeException("Kaks sama kuupäevaga EventDateContainerit!");
                }).orElse(null);

        if (con == null) {
            try {
                con = new EventDateContainerController();
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getClassLoader()
                        .getResource("EventDateContainer.fxml"));
                loader.setRoot(con);
                loader.setController(con);
                loader.load();
                con.setLdate(e.getAlgushetk().toLocalDate());
                con.setParent(this);
                con.setEventTeenus(eventTeenus);
                con.setCurrentGroup(selectedGroup);
                container.getChildren().add(con);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        con.addEvent(e);
    }

    public void remove(EventDateContainerController c) {
        container.getChildren().remove(c);
    }
}

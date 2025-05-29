package oop.tegevusteplaneerija.client;

import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import oop.tegevusteplaneerija.client.util.ClientDBManager;
import oop.tegevusteplaneerija.common.mudel.CalendarEvent;
import oop.tegevusteplaneerija.common.mudel.Grupp;
import oop.tegevusteplaneerija.common.teenused.EventTeenus;
import oop.tegevusteplaneerija.common.teenused.GrupiTeenus;
import oop.tegevusteplaneerija.common.util.AndmeHaldus;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class DatesContainerController {
    @FXML
    private VBox container;

    @FXML
    private BorderPane bPane;

    @FXML
    private Label helloLabel;
    @FXML
    private Button refreshButton;

    private List<Grupp> allGroups = new ArrayList<>();
    private Grupp selectedGroup;
    private GrupiTeenus grupiTeenus;
    private EventTeenus eventTeenus;
    private ClientDBManager dbManager;
    private oop.tegevusteplaneerija.common.mudel.Kasutaja activeUser;
    private oop.tegevusteplaneerija.common.teenused.KasutajaTeenus kasutajaTeenus;

    public void värskendaGrupid() {
        if (activeUser == null) {
            return; // shrug
        }
        int userId = activeUser.getId();
        try {
            allGroups = new ArrayList<>(new LinkedHashSet<>(grupiTeenus.leiaKasutajaGrupid(userId)));
            if (!allGroups.isEmpty()) {
                selectedGroup = allGroups.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDBManager(ClientDBManager dbManager) throws SQLException {
        this.dbManager = dbManager;
        AndmeHaldus temp = new AndmeHaldus(dbManager);
        this.eventTeenus = new EventTeenus(temp);
        this.grupiTeenus = new GrupiTeenus(temp);
    }

    public void setSelectedGroup(Grupp group) {
        this.selectedGroup = group;
    }

    public void setActiveUser(oop.tegevusteplaneerija.common.mudel.Kasutaja user) {
        this.activeUser = user;
        // Only refresh if all dependencies are set
        if (dbManager != null && grupiTeenus != null && eventTeenus != null) {
            refreshAll();
        }
        if (helloLabel != null && user != null) {
            helloLabel.setText("Hello " + user.getNimi());
        }
    }

    public void setKasutajaTeenus(oop.tegevusteplaneerija.common.teenused.KasutajaTeenus kasutajaTeenus) {
        this.kasutajaTeenus = kasutajaTeenus;
    }

    @FXML
    private void initialize() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem newEventItem = new MenuItem("Add Event");
        newEventItem.setOnAction(actionEvent -> {
            try {
                var dialog = new EventDialog(null, (Stage) container.getScene().getWindow());
                EventDialogController controller = dialog.getFxmlLoader().getController();
                controller.setGroups(allGroups, selectedGroup);
                var eventResult = dialog.waitForResult();
                if (eventResult != null)
                    addEvent(eventResult);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        MenuItem newGroupItem = new MenuItem("Create Group");
        newGroupItem.setOnAction(actionEvent -> showCreateGroupDialog());
        contextMenu.getItems().addAll(newEventItem, newGroupItem);
        bPane.setOnContextMenuRequested(e -> {
            contextMenu.show(bPane.getScene().getWindow(), e.getScreenX(), e.getScreenY());
        });

        // Set hello label
        if (activeUser != null) {
            helloLabel.setText("Hello " + activeUser.getNimi());
        }
        refreshButton.setOnAction(e -> refreshAll());

    }

    private void showCreateGroupDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("UserGroupSelector.fxml"));
            VBox root = loader.load();
            UserGroupSelectorController controller = loader.getController();
            Stage dialog = new Stage();
            dialog.setTitle("Create Group");
            dialog.setScene(new Scene(root));
            dialog.initOwner(container.getScene().getWindow());
            dialog.showAndWait();
            if (!controller.isCancelled()) {
                String groupName = controller.getGroupName();
                List<String> usernames = controller.getUsernames();
                int groupId = grupiTeenus.looKoostööGrupp(groupName, activeUser, List.of()).getId();
                Grupp group = grupiTeenus.leiaGrupp(groupId);
                for (String username : usernames) {
                    var user = kasutajaTeenus.leiaKasutaja(username);
                    if (user != null) {
                        grupiTeenus.lisaGrupiLiige(group, user);
                    }
                }
                värskendaGrupid();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void refreshAll() {
        if (grupiTeenus != null && activeUser != null) {
            try {
                dbManager.refreshDatabase();
                värskendaGrupid();
                // Remove all event containers from the UI
                container.getChildren().clear();
                // Re-add all events for all groups (or just the selected group)
                if (eventTeenus != null) {
                    List<CalendarEvent> events = eventTeenus.leiaKõikSündmused();
                    for (CalendarEvent event : events) {
                        Grupp group = event.getGrupp();
                        Grupp fullGroup = group;
                        if (group != null && (group.getNimi() == null || group.getNimi().isEmpty())) {
                            try {
                                Grupp fetched = grupiTeenus.leiaGrupp(group.getId());
                                if (fetched != null) {
                                    fullGroup = fetched;
                                }
                            } catch (Exception ignored) {
                            }
                        }
                        // Only add to view if group is not null and has a name
                        if (fullGroup != null && fullGroup.getNimi() != null && !fullGroup.getNimi().isEmpty()) {
                            CalendarEvent eventForView = new CalendarEvent(event.getNimi(), event.getKirjeldus(),
                                    event.getAlgushetk(), event.getLopphetk(), fullGroup);
                            addEventToView(eventForView);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void addEvent(CalendarEvent e) {
        addEventToDatabase(e);
        addEventToView(e);
    }

    public void addEventToView(CalendarEvent e) {
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
                con.setActiveUser(activeUser); // <-- set active user
                container.getChildren().add(con);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            con.setActiveUser(activeUser); // <-- set active user for existing container
        }

        con.addEvent(e);
    }

    public void addEventToDatabase(CalendarEvent e) {
        if (eventTeenus != null && (e.getGrupp() != null) && (e.getId() < 0)) {
            try {
                // Use the group from the event, not always selectedGroup
                var dbEvent = new CalendarEvent(e.getNimi(), e.getKirjeldus(), e.getAlgushetk(), e.getLopphetk(),
                        e.getGrupp());
                eventTeenus.lisaSündmus(dbEvent);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void remove(EventDateContainerController c) {
        container.getChildren().remove(c);
    }

    public GrupiTeenus getGrupiTeenus() {
        return grupiTeenus;
    }
}

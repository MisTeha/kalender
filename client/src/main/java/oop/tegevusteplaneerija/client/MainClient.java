package oop.tegevusteplaneerija.client;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.*;
import oop.tegevusteplaneerija.client.util.ClientDBManager;
import oop.tegevusteplaneerija.common.mudel.CalendarEvent;
import oop.tegevusteplaneerija.common.mudel.Grupp;
import oop.tegevusteplaneerija.common.mudel.Kasutaja;
import oop.tegevusteplaneerija.common.teenused.EventTeenus;
import oop.tegevusteplaneerija.common.teenused.GrupiTeenus;
import oop.tegevusteplaneerija.common.teenused.KasutajaTeenus;
import oop.tegevusteplaneerija.common.util.AndmeHaldus;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.io.IOException;
import java.util.List;

public class MainClient extends Application {

    private String SERVER_URL = "localhost:8080";
    private String DB_PATH = "client.db";

    private AndmeHaldus andmeHaldus;
    private EventTeenus eventTeenus;
    private GrupiTeenus grupiTeenus;
    private KasutajaTeenus kasutajaTeenus;
    private ClientDBManager dbManager;

    public static void main(String[] args) {
        launch(args);
    }

    private void startDatabase() throws SQLException {
        dbManager = new ClientDBManager(DB_PATH, SERVER_URL, null);
        dbManager.init();
        andmeHaldus = new AndmeHaldus(dbManager);
        eventTeenus = new EventTeenus(andmeHaldus);
        grupiTeenus = new GrupiTeenus(andmeHaldus);
        kasutajaTeenus = new KasutajaTeenus(andmeHaldus);
    }

    @Override
    public void start(Stage primaryStage) throws SQLException, IOException {
        startDatabase();
        showLoginScreen(primaryStage);
    }

    private void showLoginScreen(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("LoginScreen.fxml"));
        Pane loginPane = loader.load();
        LoginController controller = loader.getController();
        if (controller == null) {
            controller = (LoginController) loginPane.getProperties().get("fx:controller");
        }
        controller.setDbManager(dbManager);
        controller.setLoginCallback(user -> {
            try {
                showMainUI(stage, user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Scene scene = new Scene(loginPane);
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.setWidth(400);
        stage.setHeight(300);
        stage.show();
    }

    private void showMainUI(Stage primaryStage, Kasutaja kasutaja) throws SQLException, IOException {
        dbManager.setUserId(kasutaja.getId());
        Grupp huh = dbManager.leiaPersonaalneGrupp(4);
        Grupp huh2 = dbManager.leiaPersonaalneGrupp(3);
        Grupp personalGrupp = grupiTeenus.leiaPersonaalneGrupp(kasutaja);
        List<CalendarEvent> events = eventTeenus.leiaKõikSündmused();
        if (events.isEmpty()) {
            CalendarEvent event1 = new CalendarEvent("Client Meeting", "Discuss UI design",
                    java.time.ZonedDateTime.parse("2025-04-01T13:00:00+02:00"),
                    java.time.ZonedDateTime.parse("2025-04-01T14:00:00+02:00"), personalGrupp);
            CalendarEvent event2 = new CalendarEvent("foo", "bar",
                    java.time.ZonedDateTime.parse("2025-04-02T13:00:00+02:00"),
                    java.time.ZonedDateTime.parse("2025-04-02T14:00:00+02:00"), personalGrupp);
            eventTeenus.lisaSündmus(event1);
            eventTeenus.lisaSündmus(event2);
            events = eventTeenus.leiaKõikSündmused();
        }
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("DatesContainer.fxml"));
        Pane dateContainer = loader.load();
        DatesContainerController cont = loader.getController();
        cont.setDBManager(dbManager);
        cont.setSelectedGroup(personalGrupp);
        cont.setActiveUser(kasutaja);
        Scene scene = new Scene(dateContainer);
        dateContainer.prefHeightProperty().bind(scene.heightProperty());
        dateContainer.prefWidthProperty().bind(scene.widthProperty());
        primaryStage.setTitle("Calendar Client");
        primaryStage.setScene(scene);
        primaryStage.setHeight(600);
        primaryStage.setWidth(600);
        primaryStage.show();
    }
}

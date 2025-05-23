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

    public static void main(String[] args) {
        launch(args);
    }

    private static EventTeenus eventTeenus;
    private static GrupiTeenus grupiTeenus;
    private static KasutajaTeenus kasutajaTeenus;

    private static void startDatabase() throws SQLException {
        String DB_PATH = "client.db";
        String SERVER_URL = "localhost:8080";
        ClientDBManager dbManager = new ClientDBManager(DB_PATH, SERVER_URL, 4);
        dbManager.init();
        dbManager.refreshDatabase();
        AndmeHaldus andmeHaldus = new AndmeHaldus(dbManager);
        eventTeenus = new EventTeenus(andmeHaldus);
        grupiTeenus = new GrupiTeenus(andmeHaldus);
        kasutajaTeenus = new KasutajaTeenus(andmeHaldus);
    }

    @Override
    public void start(Stage primaryStage) throws SQLException, IOException {
        startDatabase();
        // avab niiöelda demokasutaja, kuna pole veel implementeeritud kasutaja
        // tegemist/valimist.
        Kasutaja kasutaja = kasutajaTeenus.leiaKasutaja(1);
        if (kasutaja == null) {
            kasutaja = kasutajaTeenus.looKasutaja("DemoKasutaja");
            kasutajaTeenus.lisaPersonaalneGrupp(kasutaja);
        }
        Grupp personalGrupp = grupiTeenus.leiaPersonaalneGrupp(kasutaja);
        // Näidissündmused juhul kui on nn esimene käivitus või kalender tühi.
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
        cont.setEventTeenus(eventTeenus);
        cont.setGroupTeenus(grupiTeenus, kasutaja.getId());
        cont.setSelectedGroup(personalGrupp);
        Scene scene = new Scene(dateContainer);
        dateContainer.prefHeightProperty().bind(scene.heightProperty());
        dateContainer.prefWidthProperty().bind(scene.widthProperty());

        events.forEach(cont::addEvent);
        primaryStage.setTitle("Calendar Client");
        primaryStage.setScene(scene);
        primaryStage.setHeight(600);
        primaryStage.setWidth(600);
        primaryStage.show();
    }
}

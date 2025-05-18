package oop.tegevusteplaneerija.server;

import com.google.gson.Gson;
import oop.tegevusteplaneerija.common.mudel.CalendarEvent;
import oop.tegevusteplaneerija.common.mudel.Grupp;
import oop.tegevusteplaneerija.common.mudel.Kasutaja;
import oop.tegevusteplaneerija.common.teenused.EventTeenus;
import oop.tegevusteplaneerija.common.teenused.GrupiTeenus;
import oop.tegevusteplaneerija.common.teenused.KasutajaTeenus;
import oop.tegevusteplaneerija.common.util.AndmeHaldus;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;

public class MainServer {
    public static void main(String[] args) {
        String envPort = System.getenv("PORT"); // azure teenuses on olemas
        int portnumber = envPort != null ? Integer.parseInt(envPort) : 8080;
        port(portnumber);

        final AndmeHaldus andmeHaldus;
        final EventTeenus eventTeenus;
        final GrupiTeenus grupiTeenus;
        final KasutajaTeenus kasutajaTeenus;
        try {
            andmeHaldus = new AndmeHaldus("server.db");
            eventTeenus = new EventTeenus("server.db");
            grupiTeenus = new GrupiTeenus(andmeHaldus);
            kasutajaTeenus = new KasutajaTeenus(andmeHaldus);
        } catch (SQLException e) {
            e.printStackTrace();
            stop();
            return;
        }

        Gson gson = new Gson();

        get("/events", (req, res) -> {
            res.type("application/json");
            try {
                return eventTeenus.leiaKõikSündmused();
            } catch (SQLException e) {
                res.status(540);
                return e.getMessage();
            }
        }, gson::toJson);

        get("/tere", (req, res) -> {
            res.type("application/json");
            return "teremain";
        }, gson::toJson);

        post("/events", (req, res) -> {
            res.type("application/json");
            try {
                CalendarEvent event = gson.fromJson(req.body(), CalendarEvent.class);
                if (event == null) {
                    System.out.println("event on null");
                    res.status(400);
                    return "Invalid event data";
                }
                int id = eventTeenus.lisaSündmus(event);
                event = eventTeenus.leiaSündmus(id);
                res.status(201); // Created
                return event;
            } catch (SQLException e) {
                res.status(520);
                return e.getMessage();
            }
        }, gson::toJson);


        get("/showcase", (req, res) -> {
            res.type("application/json");
            try {
                Kasutaja kasutaja = kasutajaTeenus.looKasutaja("Testkasutaja");
                Grupp grupp = grupiTeenus.looKoostööGrupp("Testgrupp", kasutaja, List.of(kasutaja));
                CalendarEvent event = new CalendarEvent("Test Event", "Kirjeldus", java.time.ZonedDateTime.now(),
                        java.time.ZonedDateTime.now().plusHours(1), grupp);
                int eventId = eventTeenus.lisaSündmus(event);
                List<CalendarEvent> groupEvents = eventTeenus.leiaGrupiSündmused(grupp.getId());
                List<Grupp> userGroups = grupiTeenus.leiaKasutajaGrupid(kasutaja.getId());
                List<CalendarEvent> userEvents = eventTeenus.leiaKasutajaSündmused(kasutaja.getId());
                CalendarEvent fetchedEvent = eventTeenus.leiaSündmus(eventId);
                eventTeenus.kustutaSündmus(fetchedEvent);
                grupiTeenus.kustutaGrupp(grupp);
                kasutajaTeenus.kustutaKasutaja(kasutaja);
                return Map.of(
                        "createdUser", kasutaja,
                        "createdGroup", grupp,
                        "createdEvent", event,
                        "groupEvents", groupEvents,
                        "userGroups", userGroups,
                        "userEvents", userEvents,
                        "fetchedEvent", fetchedEvent);
            } catch (Exception e) {
                res.status(500);
                throw(e);
                //return Map.of("error", e.getMessage());
            }
        }, gson::toJson);
    }
}

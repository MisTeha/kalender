package oop.tegevusteplaneerija.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import oop.tegevusteplaneerija.common.mudel.CalendarEvent;
import oop.tegevusteplaneerija.common.mudel.Grupp;
import oop.tegevusteplaneerija.common.mudel.Kasutaja;
import oop.tegevusteplaneerija.common.teenused.EventTeenus;
import oop.tegevusteplaneerija.common.teenused.GrupiTeenus;
import oop.tegevusteplaneerija.common.teenused.KasutajaTeenus;
import oop.tegevusteplaneerija.common.util.AndmeHaldus;
import oop.tegevusteplaneerija.common.util.DatabaseManager;
import oop.tegevusteplaneerija.server.util.ServerDBManager;

import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
            DatabaseManager dbManager = new ServerDBManager("server.db");
            andmeHaldus = new AndmeHaldus(dbManager);
            eventTeenus = new EventTeenus(andmeHaldus);
            grupiTeenus = new GrupiTeenus(andmeHaldus);
            kasutajaTeenus = new KasutajaTeenus(andmeHaldus);
        } catch (SQLException e) {
            e.printStackTrace();
            stop();
            return;
        }

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ZonedDateTime.class,
                        (JsonSerializer<ZonedDateTime>) (src, typeOfSrc,
                                context) -> new com.google.gson.JsonPrimitive(src.toString()))
                .registerTypeAdapter(ZonedDateTime.class,
                        (JsonDeserializer<ZonedDateTime>) (json, type, context) -> ZonedDateTime
                                .parse(json.getAsString()))
                .registerTypeAdapter(ZoneId.class,
                        (JsonSerializer<ZoneId>) (src, typeOfSrc,
                                context) -> new com.google.gson.JsonPrimitive(src.getId()))
                .registerTypeAdapter(ZoneId.class,
                        (JsonDeserializer<ZoneId>) (json, type, context) -> ZoneId.of(json.getAsString()))
                .create();

        // --- EVENT ENDPOINTS ---

        get("/events", (req, res) -> {
            res.type("application/json");
            try {
                return eventTeenus.leiaKõikSündmused();
            } catch (SQLException e) {
                res.status(540);
                return e.getMessage();
            }
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
                int id = eventTeenus.lisaSündmus(event).getId();
                // kontroll, kas sündmus tuli.
                event = eventTeenus.leiaSündmus(id);
                res.status(201); // tehtud
                return event;
            } catch (SQLException e) {
                res.status(520);
                return e.getMessage();
            }
        }, gson::toJson);

        // Get event by id
        get("/events/:id", (req, res) -> {
            res.type("application/json");
            int id = Integer.parseInt(req.params(":id"));
            return eventTeenus.leiaSündmus(id);
        }, gson::toJson);

        // Get events by group
        get("/groups/:id/events", (req, res) -> {
            res.type("application/json");
            int groupId = Integer.parseInt(req.params(":id"));
            return eventTeenus.leiaGrupiSündmused(groupId);
        }, gson::toJson);

        // Delete event
        delete("/events/:id", (req, res) -> {
            res.type("application/json");
            int id = Integer.parseInt(req.params(":id"));
            CalendarEvent event = eventTeenus.leiaSündmus(id);
            if (event != null) {
                eventTeenus.kustutaSündmus(event);
                res.status(204);
                return "";
            } else {
                res.status(404);
                return "Event not found";
            }
        });

        // --- GROUP ENDPOINTS ---
        // Get group by id
        get("/groups/:id", (req, res) -> {
            res.type("application/json");
            int id = Integer.parseInt(req.params(":id"));
            return grupiTeenus.leiaGrupp(id);
        }, gson::toJson);

        // Get all groups for a user
        get("/users/:id/groups", (req, res) -> {
            res.type("application/json");
            int userId = Integer.parseInt(req.params(":id"));
            return grupiTeenus.leiaKasutajaGrupid(userId);
        }, gson::toJson);

        // Create group
        post("/groups", (req, res) -> {
            res.type("application/json");
            try {
                Map<String, Object> data = gson.fromJson(req.body(), Map.class);
                String nimi = (String) data.get("nimi");
                int omanikId = ((Double) data.get("omanikId")).intValue();
                List<Double> liikmedIds = (List<Double>) data.get("liikmed");
                Kasutaja omanik = kasutajaTeenus.leiaKasutaja(omanikId);
                List<Kasutaja> liikmed = new java.util.ArrayList<>();
                for (Double id : liikmedIds) {
                    liikmed.add(kasutajaTeenus.leiaKasutaja(id.intValue()));
                }
                Grupp grupp = grupiTeenus.looKoostööGrupp(nimi, omanik, liikmed);
                res.status(201);
                return grupp;
            } catch (Exception e) {
                res.status(520);
                return e.getMessage();
            }
        }, gson::toJson);

        // Delete group
        delete("/groups/:id", (req, res) -> {
            res.type("application/json");
            int id = Integer.parseInt(req.params(":id"));
            Grupp grupp = grupiTeenus.leiaGrupp(id);
            if (grupp != null) {
                grupiTeenus.kustutaGrupp(grupp);
                res.status(204);
                return "";
            } else {
                res.status(404);
                return "Group not found";
            }
        });

        // Add member to group
        post("/groups/:id/members", (req, res) -> {
            res.type("application/json");
            int groupId = Integer.parseInt(req.params(":id"));
            Map<String, Object> data = gson.fromJson(req.body(), Map.class);
            int userId = ((Double) data.get("userId")).intValue();
            Grupp grupp = grupiTeenus.leiaGrupp(groupId);
            Kasutaja kasutaja = kasutajaTeenus.leiaKasutaja(userId);
            grupiTeenus.lisaGrupiLiige(grupp, kasutaja);
            res.status(201);
            return "";
        });

        // Remove member from group
        delete("/groups/:groupId/members/:userId", (req, res) -> {
            res.type("application/json");
            int groupId = Integer.parseInt(req.params(":groupId"));
            int userId = Integer.parseInt(req.params(":userId"));
            Grupp grupp = grupiTeenus.leiaGrupp(groupId);
            Kasutaja kasutaja = kasutajaTeenus.leiaKasutaja(userId);
            grupiTeenus.kustutaGrupiLiige(grupp, kasutaja);
            res.status(204);
            return "";
        });

        // Get group members
        get("/groups/:id/members", (req, res) -> {
            res.type("application/json");
            int groupId = Integer.parseInt(req.params(":id"));
            return grupiTeenus.leiaGrupiLiikmed(groupId);
        }, gson::toJson);

        // --- USER ENDPOINTS ---
        // Get user by id
        get("/users/:id", (req, res) -> {
            res.type("application/json");
            int id = Integer.parseInt(req.params(":id"));
            return kasutajaTeenus.leiaKasutaja(id);
        }, gson::toJson);

        // Get user by name
        get("/users/byname/:name", (req, res) -> {
            res.type("application/json");
            String name = req.params(":name");
            return kasutajaTeenus.leiaKasutaja(name);
        }, gson::toJson);

        // Create user
        post("/users", (req, res) -> {
            res.type("application/json");
            try {
                Map<String, Object> data = gson.fromJson(req.body(), Map.class);
                String nimi = (String) data.get("nimi");
                Kasutaja kasutaja = kasutajaTeenus.looKasutaja(nimi);
                res.status(201);
                kasutaja.setPersonalGrupp(null); // muidu hakkab gson ringlema
                return kasutaja;
            } catch (Exception e) {
                res.status(520);
                return e.getMessage();
            }
        }, gson::toJson);

        // Delete user
        delete("/users/:id", (req, res) -> {
            res.type("application/json");
            int id = Integer.parseInt(req.params(":id"));
            Kasutaja kasutaja = kasutajaTeenus.leiaKasutaja(id);
            if (kasutaja != null) {
                kasutajaTeenus.kustutaKasutaja(kasutaja);
                res.status(204);
                return "";
            } else {
                res.status(404);
                return "User not found";
            }
        });
    }
}

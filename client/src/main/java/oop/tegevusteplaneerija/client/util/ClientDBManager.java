package oop.tegevusteplaneerija.client.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import oop.tegevusteplaneerija.common.mudel.CalendarEvent;
import oop.tegevusteplaneerija.common.mudel.Grupp;
import oop.tegevusteplaneerija.common.mudel.Kasutaja;
import oop.tegevusteplaneerija.common.util.DatabaseManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;

/**
 * Siia klassi tulevad eraldi SQL statementid ja kood, mis lihtsalt kustutab
 * kliendi andmebaasi
 * andmed ära ning küsib serverilt uued.
 * Lisaks tuleb siia järgnev loogika andmete uuendamisel:
 * grupi/sündmuse/kasutaja/grupiliikme lisamisel või kustutamisel kõigepealt
 * saadetakse request serveri
 * endpointile ning siis refreshitakse terve kliendi andmebaas serveri
 * andmetega.
 */
public class ClientDBManager extends DatabaseManager {

    private final String serverUrl;
    private Integer userId;
    private final HashMap<Integer, String> viimatiNähtudKasutajad;
    private final Set<Grupp> viimatiNähtudGrupid;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(int uus) {
        this.userId = uus;
        refreshDatabase();
    }

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(java.time.ZonedDateTime.class,
                    (com.google.gson.JsonDeserializer<java.time.ZonedDateTime>) (json, type,
                            context) -> java.time.ZonedDateTime.parse(json.getAsString()))
            .registerTypeAdapter(java.time.ZonedDateTime.class,
                    (com.google.gson.JsonSerializer<java.time.ZonedDateTime>) (src, typeOfSrc,
                            context) -> new com.google.gson.JsonPrimitive(src.toString()))
            .registerTypeAdapter(java.time.ZoneId.class,
                    (com.google.gson.JsonDeserializer<java.time.ZoneId>) (json, type, context) -> java.time.ZoneId
                            .of(json.getAsString()))
            .registerTypeAdapter(java.time.ZoneId.class,
                    (com.google.gson.JsonSerializer<java.time.ZoneId>) (src, typeOfSrc,
                            context) -> new com.google.gson.JsonPrimitive(src.getId()))
            .create();

    public ClientDBManager(String dbPath, String serverUrl, Integer userId) {
        this.dbPath = "jdbc:sqlite:" + dbPath;
        this.serverUrl = serverUrl;
        this.userId = userId;
        this.viimatiNähtudGrupid = new HashSet<>();
        this.viimatiNähtudKasutajad = new HashMap<>();
    }

    public void refreshDatabase() {
        refreshGrupid();
        refreshKasutajad();
        refreshSündmused();
    }

    private void refreshKasutajad() {
        clearKasutajad();
        try {
            viimatiNähtudKasutajad
                    .forEach((id, nimi) -> executeUpdate("INSERT INTO kasutajad(id, nimi) VALUES (" + id + ", '"
                            + nimi.replace("'", "''") + "')"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshGrupid() {
        clearGrupid();
        try {
            Set<Grupp> uniqueGroups = new HashSet<>();
            Grupp[] grupid = makeRequest("/users/" + userId + "/groups", Grupp[].class);
            uniqueGroups.addAll(Arrays.asList(grupid));
            viimatiNähtudKasutajad.clear();
            viimatiNähtudGrupid.clear();
            viimatiNähtudGrupid.addAll(uniqueGroups);
            for (Grupp g : uniqueGroups) {
                executeUpdate("INSERT INTO grupid(id, nimi, omanik, personal) VALUES (" + g.getId() + ", '"
                        + g.getNimi().replace("'", "''") + "', "
                        + (g.getOmanik() != null ? g.getOmanik().getId() : "NULL")
                        + ", " + (g.isPersonal() ? 1 : 0)
                        + ")");
                Kasutaja[] liikmed = makeRequest("/groups/" + g.getId() + "/members", Kasutaja[].class);
                Arrays.stream(liikmed).forEach(k -> viimatiNähtudKasutajad.put(k.getId(), k.getNimi()));
                for (Kasutaja liige : liikmed) {
                    executeUpdate(
                            "INSERT INTO grupiliikmed(grupp, liige) VALUES (" + g.getId() + ", " + liige.getId() + ")");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshSündmused() {
        clearSündmused();
        try {
            Set<CalendarEvent> allEvents = new HashSet<>();
            for (Grupp g : viimatiNähtudGrupid) {
                CalendarEvent[] events = makeRequest("/groups/" + g.getId() + "/events", CalendarEvent[].class);
                allEvents.addAll(Arrays.asList(events));
            }
            for (CalendarEvent e : allEvents) {
                executeUpdate("INSERT INTO events(id, nimi, kirjeldus, algushetk, lopphetk, grupp) VALUES ("
                        + e.getId() + ", '" + e.getNimi().replace("'", "''") + "', '"
                        + (e.getKirjeldus() != null ? e.getKirjeldus().replace("'", "''") : "") + "', '"
                        + java.sql.Timestamp.from(e.getAlgushetk().toInstant()) + "', '"
                        + java.sql.Timestamp.from(e.getLopphetk().toInstant()) + "', " + e.getGrupp().getId() + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // mulle ei meeldi konstantsed stringid
    private enum HTTPMeetod {
        POST, DELETE
    }

    private String sendRequest(String endpoint, HTTPMeetod method, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://" + serverUrl + endpoint));
        if (method == HTTPMeetod.POST) {
            builder.header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body != null ? body : ""));
        } else if (method == HTTPMeetod.DELETE) {
            builder.DELETE();
        }
        HttpRequest request = builder.build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private boolean isJsonObject(String s) {
        return s != null && s.trim().startsWith("{");
    }

    public Kasutaja loginKasutaja(String nimi) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + serverUrl + "/users/byname/" + nimi))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            if (isJsonObject(body)) {
                Kasutaja kasutaja = gson.fromJson(body, Kasutaja.class);
                return kasutaja;
            } else {
                return null;
            }
        } catch (Exception e) {
            // Not found or error
            return null;
        }
    }

    @Override
    public int lisaSündmus(String nimi, String kirjeldus, ZonedDateTime algushetk, ZonedDateTime lopphetk, int grupp) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("nimi", nimi);
            event.put("kirjeldus", kirjeldus);
            // Serialize ZonedDateTime as ISO-8601 string for server compatibility
            event.put("algushetk", algushetk != null ? algushetk.toString() : null);
            event.put("lopphetk", lopphetk != null ? lopphetk.toString() : null);
            // Send Grupp as an object with only the id field set
            Grupp groupObj = new Grupp(grupp, null, null, null, false);
            event.put("grupp", groupObj);
            String json = gson.toJson(event);
            String responseBody = sendRequest("/events", HTTPMeetod.POST, json);
            refreshDatabase();
            if (isJsonObject(responseBody)) {
                CalendarEvent created = gson.fromJson(responseBody, CalendarEvent.class);
                return created != null ? created.getId() : 0;
            } else {
                System.out.println("ERROR lisasündmuses: \n" + responseBody);
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void kustutaSündmus(int eventId) {
        try {
            sendRequest("/events/" + eventId, HTTPMeetod.DELETE, null);
            refreshDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int lisaGrupp(String nimi, int omanikId, boolean personal) {
        try {
            Map<String, Object> group = new HashMap<>();
            group.put("nimi", nimi);
            group.put("omanikId", omanikId);
            group.put("liikmed", new ArrayList<Integer>()); // initially empty
            String json = gson.toJson(group);
            String responseBody = sendRequest("/groups", HTTPMeetod.POST, json);
            refreshDatabase();
            if (isJsonObject(responseBody)) {
                Grupp created = gson.fromJson(responseBody, Grupp.class);
                return created != null ? created.getId() : 0;
            } else {
                // Optionally log or handle error message: responseBody
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void kustutaGrupp(int gruppId) {
        try {
            sendRequest("/groups/" + gruppId, HTTPMeetod.DELETE, null);
            refreshDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void lisaGrupiLiige(int gruppId, int liigeId) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("userId", liigeId);
            String json = gson.toJson(data);
            sendRequest("/groups/" + gruppId + "/members", HTTPMeetod.POST, json);
            refreshDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void kustutaGrupiLiige(int gruppId, int liigeId) {
        try {
            sendRequest("/groups/" + gruppId + "/members/" + liigeId, HTTPMeetod.DELETE, null);
            refreshDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int lisaKasutaja(String nimi) {
        try {
            Map<String, Object> user = new HashMap<>();
            user.put("nimi", nimi);
            String json = gson.toJson(user);
            String responseBody = sendRequest("/users", HTTPMeetod.POST, json);
            refreshDatabase();
            if (isJsonObject(responseBody)) {
                Kasutaja created = gson.fromJson(responseBody, Kasutaja.class);
                return created != null ? created.getId() : 0;
            } else {
                // Optionally log or handle error message: responseBody
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void kustutaKasutaja(int kasutajaId) {
        try {
            sendRequest("/users/" + kasutajaId, HTTPMeetod.DELETE, null);
            refreshDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int executeUpdate(String sql) {
        try (Connection conn = DriverManager.getConnection(dbPath);
                Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);
            int result = stmt.executeUpdate(sql);
            conn.commit();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private <T> T[] makeRequest(String endpoint, Class<T[]> sihtKlass) throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + serverUrl + endpoint))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), sihtKlass);
        }
    }

    private void clearKasutajad() {
        executeUpdate("DELETE FROM kasutajad;");
    }

    private void clearGrupid() {
        executeUpdate("DELETE FROM grupiliikmed;");
        executeUpdate("DELETE FROM grupid;");
    }

    private void clearSündmused() {
        executeUpdate("DELETE FROM events;");
    }
}

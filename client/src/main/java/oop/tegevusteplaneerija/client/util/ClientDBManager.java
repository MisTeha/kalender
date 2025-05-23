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
    private final Integer userId;
    private final HashMap<Integer, String> viimatiNähtudKasutajad;
    private final Set<Grupp> viimatiNähtudGrupid;

    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(java.time.ZonedDateTime.class,
            (com.google.gson.JsonDeserializer<java.time.ZonedDateTime>) (json, type, context) -> java.time.ZonedDateTime.parse(json.getAsString()))
        .registerTypeAdapter(java.time.ZonedDateTime.class,
            (com.google.gson.JsonSerializer<java.time.ZonedDateTime>) (src, typeOfSrc, context) -> new com.google.gson.JsonPrimitive(src.toString()))
        .registerTypeAdapter(java.time.ZoneId.class,
            (com.google.gson.JsonDeserializer<java.time.ZoneId>) (json, type, context) -> java.time.ZoneId.of(json.getAsString()))
        .registerTypeAdapter(java.time.ZoneId.class,
            (com.google.gson.JsonSerializer<java.time.ZoneId>) (src, typeOfSrc, context) -> new com.google.gson.JsonPrimitive(src.getId()))
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
            viimatiNähtudKasutajad.forEach((id, nimi) ->
                    executeUpdate("INSERT INTO kasutajad(id, nimi) VALUES (" + id + ", '"
                                  + nimi.replace("'", "''") + "')")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshGrupid() {
        clearGrupid();
        try {
            Grupp[] grupid = makeRequest("/users/" + userId + "/groups", Grupp[].class);
            viimatiNähtudKasutajad.clear();
            viimatiNähtudGrupid.clear();
            //Kasutan seti, kuna järjekord pole tähtis... duplikaadid teine probleem, kuna Grupp on objekt,
            //kuid duplikaate ei tohiks olla.
            viimatiNähtudGrupid.addAll(Arrays.asList(grupid));
            for (Grupp g : grupid) {
                executeUpdate("INSERT INTO grupid(id, nimi, omanik, personal) VALUES (" + g.getId() + ", '"
                              + g.getNimi().replace("'", "''") + "', "
                              + (g.getOmanik() != null ? g.getOmanik()
                        .getId() : "NULL") + ", " + (g.isPersonal() ? 1 : 0)
                              + ")");
                Kasutaja[] liikmed = makeRequest("/groups/" + g.getId() + "/members", Kasutaja[].class);
                //Kasutan mappi, et id'd ei korduks. Ei tee loopi sees, et exceptionid ei teeks imelikke asju.
                Arrays.stream(liikmed).forEach(k -> viimatiNähtudKasutajad.put(k.getId(), k.getNimi()));
                for (Kasutaja liige : liikmed) {
                    executeUpdate("INSERT INTO grupiliikmed(grupp, liige) VALUES (" + g.getId() + ", " + liige.getId() + ")");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshSündmused() {
        clearSündmused();
        try {
            for (Grupp g : viimatiNähtudGrupid) {
                CalendarEvent[] events = makeRequest("/groups/" + g.getId() + "/events", CalendarEvent[].class);
                if (events.length == 0) continue;
                for (CalendarEvent e : events) {
                    executeUpdate("INSERT INTO events(id, nimi, kirjeldus, algushetk, lopphetk, grupp) VALUES ("
                                  + e.getId() + ", '" + e.getNimi().replace("'", "''") + "', '"
                                  + (e.getKirjeldus() != null ? e.getKirjeldus().replace("'", "''") : "") + "', '"
                                  + java.sql.Timestamp.from(e.getAlgushetk().toInstant()) + "', '"
                                  + java.sql.Timestamp.from(e.getLopphetk().toInstant()) + "', " + g.getId() + ")");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int lisaSündmus(String nimi, String kirjeldus, ZonedDateTime algushetk, ZonedDateTime lopphetk, int grupp) {
        return 0;
    }

    @Override
    public void kustutaSündmus(int eventId) {

    }

    @Override
    public void uuendaSündmus(int id, String nimi, String kirjeldus, ZonedDateTime algushetk, ZonedDateTime lopphetk,
                              int grupp) {

    }

    @Override
    public int lisaGrupp(String nimi, int omanikId, boolean personal) {
        return 0;
    }

    @Override
    public void kustutaGrupp(int gruppId) {

    }

    @Override
    public void lisaGrupiLiige(int gruppId, int liigeId) {

    }

    @Override
    public void kustutaGrupiLiige(int gruppId, int liigeId) {

    }

    @Override
    public int lisaKasutaja(String nimi) {
        return 0;
    }

    @Override
    public void kustutaKasutaja(int kasutajaId) {

    }

    @Override
    public Kasutaja leiaKasutaja(int kasutajaId) {
        return null;
    }

    @Override
    public Kasutaja leiaKasutaja(String nimi) {
        return null;
    }

    @Override
    public Grupp leiaGrupp(int gruppId) {
        return null;
    }

    @Override
    public List<Integer> leiaGrupiLiikmed(int gruppId) {
        return List.of();
    }

    @Override
    public List<Integer> leiaKasutajaGrupid(int kasutajaId) {
        return List.of();
    }

    @Override
    public List<CalendarEvent> leiaGrupiSündmused(int gruppId) {
        return List.of();
    }

    @Override
    public List<CalendarEvent> leiaKasutajaSündmused(int kasutajaId) {
        return List.of();
    }

    @Override
    public List<CalendarEvent> leiaKõikSündmused() {
        return List.of();
    }

    @Override
    public CalendarEvent leiaSündmus(int eventId) {
        return null;
    }

    @Override
    public Grupp leiaPersonaalneGrupp(int omanikId) {
        return null;
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

    private <T> T executeQuery(String sql, Function<ResultSet, T> resultMapper) {
        try (Connection conn = DriverManager.getConnection(dbPath);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return resultMapper.apply(rs);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
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

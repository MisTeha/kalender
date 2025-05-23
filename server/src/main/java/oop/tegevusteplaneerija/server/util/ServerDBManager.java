package oop.tegevusteplaneerija.server.util;

import oop.tegevusteplaneerija.common.mudel.CalendarEvent;
import oop.tegevusteplaneerija.common.mudel.Grupp;
import oop.tegevusteplaneerija.common.mudel.Kasutaja;
import oop.tegevusteplaneerija.common.util.DatabaseManager;
import oop.tegevusteplaneerija.common.util.SQLStatements;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServerDBManager extends DatabaseManager {

    /**
     * "Madala taseme" objekt, mis initsialiseerib ja suhtleb andmebaasiga.
     *
     * @param dbFilePath andmebaasi faili asukoht
     */
    public ServerDBManager(String dbFilePath) {
        this.dbPath = "jdbc:sqlite:" + dbFilePath;
    }

    public int lisaSündmus(String nimi, String kirjeldus, ZonedDateTime algushetk, ZonedDateTime lopphetk, int grupp)
            throws SQLException {
        return executeUpdate(SQLStatements.ADD_EVENT, ps -> {
            ps.setString(1, nimi);
            ps.setString(2, kirjeldus);
            ps.setTimestamp(3, Timestamp.from(algushetk.toInstant()));
            ps.setTimestamp(4, Timestamp.from(lopphetk.toInstant()));
            ps.setInt(5, grupp);
        }, true);
    }

    public void kustutaSündmus(int eventId) throws SQLException {
        executeUpdate(SQLStatements.REMOVE_EVENT, ps -> ps.setInt(1, eventId), false);
    }

    public void uuendaSündmus(int id, String nimi, String kirjeldus, ZonedDateTime algushetk, ZonedDateTime lopphetk,
            int grupp) throws SQLException {
        executeUpdate(SQLStatements.UPDATE_EVENT, ps -> {
            ps.setString(1, nimi);
            ps.setString(2, kirjeldus);
            ps.setTimestamp(3, Timestamp.from(algushetk.toInstant()));
            ps.setTimestamp(4, Timestamp.from(lopphetk.toInstant()));
            ps.setInt(5, grupp);
            ps.setInt(6, id);
        }, false);
    }

    public int lisaGrupp(String nimi, int omanikId, boolean personal) throws SQLException {
        return executeUpdate(SQLStatements.ADD_GROUP, ps -> {
            ps.setString(1, nimi);
            ps.setInt(2, omanikId);
            ps.setBoolean(3, personal);
        }, true);
    }

    public void kustutaGrupp(int gruppId) throws SQLException {
        executeUpdate(SQLStatements.REMOVE_GROUP, ps -> ps.setInt(1, gruppId), false);
    }

    public void lisaGrupiLiige(int gruppId, int liigeId) throws SQLException {
        executeUpdate(SQLStatements.ADD_GROUP_MEMBER, ps -> {
            ps.setInt(1, gruppId);
            ps.setInt(2, liigeId);
        }, false);
    }

    public void kustutaGrupiLiige(int gruppId, int liigeId) throws SQLException {
        executeUpdate(SQLStatements.REMOVE_GROUP_MEMBER, ps -> {
            ps.setInt(1, gruppId);
            ps.setInt(2, liigeId);
        }, false);
    }

    public int lisaKasutaja(String nimi) throws SQLException {
        return executeUpdate(SQLStatements.ADD_USER, ps -> {
            ps.setString(1, nimi);
        }, true);
    }

    public void kustutaKasutaja(int kasutajaId) throws SQLException {
        executeUpdate(SQLStatements.REMOVE_USER, ps -> ps.setInt(1, kasutajaId), false);
    }

    public Kasutaja leiaKasutaja(int kasutajaId) throws SQLException {
        return executeQuery(SQLStatements.GET_USER,
                ps -> ps.setInt(1, kasutajaId),
                rs -> {
                    if (rs.next()) {
                        int id = rs.getInt("id");
                        String nimi = rs.getString("nimi");
                        return new Kasutaja(id, nimi, null);
                    }
                    return null;
                });
    }

    public Kasutaja leiaKasutaja(String nimi) throws SQLException {
        return executeQuery(SQLStatements.GET_USER_BY_NAME,
                ps -> ps.setString(1, nimi),
                rs -> {
                    if (rs.next()) {
                        int id = rs.getInt("id");
                        String nimiResult = rs.getString("nimi");
                        return new Kasutaja(id, nimiResult, null);
                    }
                    return null;
                });
    }

    public Grupp leiaGrupp(int gruppId) throws SQLException {
        return executeQuery(SQLStatements.GET_GROUP,
                ps -> ps.setInt(1, gruppId),
                rs -> {
                    if (rs.next()) {
                        int id = rs.getInt("id");
                        String nimi = rs.getString("nimi");
                        int omanik = rs.getInt("omanik");
                        boolean personal = rs.getBoolean("personal");
                        return new Grupp(id, nimi,
                                new oop.tegevusteplaneerija.common.mudel.Kasutaja(omanik, null, null), List.of(),
                                personal);
                    }
                    return null;
                });
    }

    public List<Integer> leiaGrupiLiikmed(int gruppId) throws SQLException {
        return executeQuery(SQLStatements.GET_GROUP_MEMBERS,
                ps -> ps.setInt(1, gruppId),
                rs -> {
                    List<Integer> liikmed = new ArrayList<>();
                    while (rs.next()) {
                        liikmed.add(rs.getInt("liige"));
                    }
                    return liikmed;
                });
    }

    public List<Integer> leiaKasutajaGrupid(int kasutajaId) throws SQLException {
        return executeQuery(SQLStatements.GET_USER_GROUPS,
                ps -> ps.setInt(1, kasutajaId),
                rs -> {
                    List<Integer> grupid = new ArrayList<>();
                    while (rs.next()) {
                        grupid.add(rs.getInt("grupp"));
                    }
                    return grupid;
                });
    }

    public List<CalendarEvent> leiaGrupiSündmused(int gruppId) throws SQLException {
        return executeQuery(SQLStatements.GET_GROUP_EVENTS,
                ps -> ps.setInt(1, gruppId),
                rs -> {
                    List<CalendarEvent> result = new ArrayList<>();
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String nimi = rs.getString("nimi");
                        String kirjeldus = rs.getString("kirjeldus");
                        ZonedDateTime algushetk = rs.getTimestamp("algushetk").toInstant()
                                .atZone(java.time.ZoneId.systemDefault());
                        ZonedDateTime lopphetk = rs.getTimestamp("lopphetk").toInstant()
                                .atZone(java.time.ZoneId.systemDefault());
                        int groupId = rs.getInt("grupp");
                        Grupp grupp = new Grupp(groupId, null, null, List.of(), false);
                        result.add(new CalendarEvent(id, nimi, kirjeldus, algushetk, lopphetk, grupp));
                    }
                    return result;
                });
    }

    public List<CalendarEvent> leiaKasutajaSündmused(int kasutajaId) throws SQLException {
        return executeQuery(SQLStatements.GET_USER_EVENTS,
                ps -> ps.setInt(1, kasutajaId),
                rs -> {
                    List<CalendarEvent> result = new ArrayList<>();
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String nimi = rs.getString("nimi");
                        String kirjeldus = rs.getString("kirjeldus");
                        ZonedDateTime algushetk = rs.getTimestamp("algushetk").toInstant()
                                .atZone(java.time.ZoneId.systemDefault());
                        ZonedDateTime lopphetk = rs.getTimestamp("lopphetk").toInstant()
                                .atZone(java.time.ZoneId.systemDefault());
                        int groupId = rs.getInt("grupp");
                        Grupp grupp = new Grupp(groupId, null, null, List.of(), false);
                        result.add(new CalendarEvent(id, nimi, kirjeldus, algushetk, lopphetk, grupp));
                    }
                    return result;
                });
    }

    public List<CalendarEvent> leiaKõikSündmused() throws SQLException {
        return executeQuery(SQLStatements.GET_ALL_EVENTS,
                (_) -> {
                },
                rs -> {
                    List<CalendarEvent> result = new ArrayList<>();
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String nimi = rs.getString("nimi");
                        String kirjeldus = rs.getString("kirjeldus");
                        ZonedDateTime algushetk = rs.getTimestamp("algushetk").toInstant()
                                .atZone(java.time.ZoneId.systemDefault());
                        ZonedDateTime lopphetk = rs.getTimestamp("lopphetk").toInstant()
                                .atZone(java.time.ZoneId.systemDefault());
                        int groupId = rs.getInt("grupp");
                        Grupp grupp = new Grupp(groupId, null, null, List.of(), false);
                        result.add(new CalendarEvent(id, nimi, kirjeldus, algushetk, lopphetk, grupp));
                    }
                    return result;
                });
    }

    public CalendarEvent leiaSündmus(int eventId) throws SQLException {
        return executeQuery(SQLStatements.GET_EVENT_BY_ID,
                ps -> ps.setInt(1, eventId),
                rs -> {
                    if (rs.next()) {
                        int id = rs.getInt("id");
                        String nimi = rs.getString("nimi");
                        String kirjeldus = rs.getString("kirjeldus");
                        ZonedDateTime algushetk = rs.getTimestamp("algushetk").toInstant()
                                .atZone(java.time.ZoneId.systemDefault());
                        ZonedDateTime lopphetk = rs.getTimestamp("lopphetk").toInstant()
                                .atZone(java.time.ZoneId.systemDefault());
                        int groupId = rs.getInt("grupp");
                        Grupp grupp = new Grupp(groupId, null, null, List.of(), false);
                        return new CalendarEvent(id, nimi, kirjeldus, algushetk, lopphetk, grupp);
                    }
                    return null;
                });
    }

    public Grupp leiaPersonaalneGrupp(int omanikId) throws SQLException {
        return executeQuery(SQLStatements.GET_PERSONAL_GROUP,
                ps -> ps.setInt(1, omanikId),
                rs -> {
                    if (rs.next()) {
                        int id = rs.getInt("id");
                        String nimi = rs.getString("nimi");
                        boolean personal = rs.getBoolean("personal");
                        return new Grupp(id, nimi, null, List.of(), personal);
                    }
                    return null;
                });
    }

    /**
     * Funktsioon, mis jooksutab consumerit, et parameetreid sättida ning siis
     * executeb statemendi.
     * Commitimine ei tundu siin väga vajalik... teeb lihtsalt asja raskemaks.
     * Järgmises analoogses
     * meetodis ei kasuta.
     *
     * @param sql                SQL "skript"
     * @param parameterSetter    consumer mis seab ps argumendid õigeks
     * @param returnGeneratedKey kas päring tagastab võtit (lisab midagi kuhugi) või
     *                           mitte.
     * @throws SQLException kui midagi valesti.
     */
    private int executeUpdate(String sql, SQLConsumer<PreparedStatement> parameterSetter, boolean returnGeneratedKey)
            throws SQLException {
        try (Connection conn = DriverManager.getConnection(dbPath);
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            conn.setAutoCommit(false);
            parameterSetter.accept(ps);
            ps.executeUpdate();
            int key = -1;
            if (returnGeneratedKey) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        key = rs.getInt(1);
                    }
                }
            }
            conn.commit();
            return key;
        }
    }

    /**
     * Analoogne eelnevale meetodile, kasutab consumerit, et parameetreid õigeks
     * seada,
     * aga ei returni mitte ResultSet'i, vaid mapperit, mis teeb ResultSetiga
     * tegevused ära
     * ning siis sulgeb selle (try-with resources'iga)
     *
     * @param sql             SQL "skript"
     * @param parameterSetter consumer mis seab ps argumendid õigeks
     * @param resultMapper    funktsioon mis kaardistab ResultSeti tulemuse
     * @throws SQLException kui midagi valesti.
     */
    private <T> T executeQuery(String sql, SQLConsumer<PreparedStatement> parameterSetter,
            SQLFunction<ResultSet, T> resultMapper) throws SQLException {
        try (Connection conn = DriverManager.getConnection(dbPath);
                PreparedStatement ps = conn.prepareStatement(sql)) {
            parameterSetter.accept(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return resultMapper.apply(rs);
            }
        }
    }

    /**
     * Funktsionaalne interface, mis võimaldab SQL päringute täitmist koos
     * parameetritega. Teeb koodi veidi lühemaks ning saan rakendada õpitud asju ;)
     */
    @FunctionalInterface
    private interface SQLConsumer<T> {
        void accept(T t) throws SQLException;
    }

    /**
     * Funktsionaalne interface, mis võimaldab SQL päringute tulemuste
     * kaardistamist.
     */
    @FunctionalInterface
    private interface SQLFunction<T, R> {
        R apply(T t) throws SQLException;
    }
}

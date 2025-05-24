package oop.tegevusteplaneerija.common.util;

import oop.tegevusteplaneerija.common.mudel.CalendarEvent;
import oop.tegevusteplaneerija.common.mudel.Grupp;
import oop.tegevusteplaneerija.common.mudel.Kasutaja;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Database manager'i interface smailifeis
 */
public abstract class DatabaseManager {
    protected String dbPath;

    public void init() throws SQLException {
        Connection conn = DriverManager.getConnection(this.dbPath);
        try {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(SQLStatements.CREATE_EVENTS_TABLE);
                stmt.execute(SQLStatements.CREATE_KASUTAJAD_TABLE);
                stmt.execute(SQLStatements.CREATE_GRUPID_TABLE);
                stmt.execute(SQLStatements.CREATE_GRUPILIIKMED_TABLE);
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }

    public abstract int lisaSündmus(String nimi, String kirjeldus, ZonedDateTime algushetk, ZonedDateTime lopphetk,
            int grupp) throws SQLException;

    public abstract void kustutaSündmus(int eventId) throws SQLException;

    public abstract void uuendaSündmus(int id, String nimi, String kirjeldus, ZonedDateTime algushetk,
            ZonedDateTime lopphetk, int grupp) throws SQLException;

    public abstract int lisaGrupp(String nimi, int omanikId, boolean personal) throws SQLException;

    public abstract void kustutaGrupp(int gruppId) throws SQLException;

    public abstract void lisaGrupiLiige(int gruppId, int liigeId) throws SQLException;

    public abstract void kustutaGrupiLiige(int gruppId, int liigeId) throws SQLException;

    public abstract int lisaKasutaja(String nimi) throws SQLException;

    public abstract void kustutaKasutaja(int kasutajaId) throws SQLException;

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
                        return new Grupp(id, nimi, new Kasutaja(omanik, null, null), List.of(), personal);
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
                (__) -> {
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
                        int omanik = rs.getInt("omanik");
                        boolean personal = rs.getBoolean("personal");
                        return new Grupp(id, nimi, new Kasutaja(omanik, null, null), List.of(), personal);
                    }
                    return null;
                });
    }

    /**
     * Analoogne ServerDBManager executeUpdate meetodile, kasutab consumerit, et
     * parameetreid õigeks
     * seada, aga ei returni mitte ResultSet'i, vaid mapperit, mis teeb ResultSetiga
     * tegevused ära
     * ning siis sulgeb selle (try-with resources'iga)
     *
     * @param sql             SQL "skript"
     * @param parameterSetter consumer mis seab ps argumendid õigeks
     * @param resultMapper    funktsioon mis kaardistab ResultSeti tulemuse
     * @throws SQLException kui midagi valesti.
     */
    protected <T> T executeQuery(String sql, SQLConsumer<PreparedStatement> parameterSetter,
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
    protected interface SQLConsumer<T> {
        void accept(T t) throws SQLException;
    }

    /**
     * Funktsionaalne interface, mis võimaldab SQL päringute tulemuste
     * kaardistamist.
     */
    @FunctionalInterface
    protected interface SQLFunction<T, R> {
        R apply(T t) throws SQLException;
    }
}

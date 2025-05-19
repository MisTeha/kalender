package oop.tegevusteplaneerija.common.util;

import oop.tegevusteplaneerija.common.mudel.CalendarEvent;
import oop.tegevusteplaneerija.common.mudel.Grupp;
import oop.tegevusteplaneerija.common.mudel.Kasutaja;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private final String url;

    /**
     * "Madala taseme" objekt, mis initsialiseerib ja suhtleb andmebaasiga.
     *
     * @param dbFilePath andmebaasi faili asukoht
     */
    protected DatabaseManager(String dbFilePath) {
        this.url = "jdbc:sqlite:" + dbFilePath;
    }

    protected void init() throws SQLException {
        Connection conn = DriverManager.getConnection(url);
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

    protected int lisaEvent(String nimi, String kirjeldus, ZonedDateTime algushetk, ZonedDateTime lopphetk, int grupp)
            throws SQLException {
        ResultSet rs = executeUpdate(SQLStatements.ADD_EVENT, ps -> {
            ps.setString(1, nimi);
            ps.setString(2, kirjeldus);
            ps.setTimestamp(3, Timestamp.from(algushetk.toInstant()));
            ps.setTimestamp(4, Timestamp.from(lopphetk.toInstant()));
            ps.setInt(5, grupp);
        });
        int saadus = rs.next() ? rs.getInt(1) : -1;
        rs.close();
        return saadus;
    }

    protected void kustutaEvent(int eventId) throws SQLException {
        executeUpdate(SQLStatements.REMOVE_EVENT, ps -> ps.setInt(1, eventId)).close();
    }

    protected void uuendaSündmus(int id, String nimi, String kirjeldus, ZonedDateTime algushetk, ZonedDateTime lopphetk,
            int grupp) throws SQLException {
        executeUpdate(SQLStatements.UPDATE_EVENT, ps -> {
            ps.setString(1, nimi);
            ps.setString(2, kirjeldus);
            ps.setTimestamp(3, Timestamp.from(algushetk.toInstant()));
            ps.setTimestamp(4, Timestamp.from(lopphetk.toInstant()));
            ps.setInt(5, grupp);
            ps.setInt(6, id);
        }).close();
    }

    protected int lisaGrupp(String nimi, int omanikId, boolean personal) throws SQLException {
        ResultSet rs = executeUpdate(SQLStatements.ADD_GROUP, ps -> {
            ps.setString(1, nimi);
            ps.setInt(2, omanikId);
            ps.setBoolean(3, personal);
        });
        int saadus = rs.next() ? rs.getInt(1) : -1;
        rs.close();
        return saadus;
    }

    protected void kustutaGrupp(int gruppId) throws SQLException {
        executeUpdate(SQLStatements.REMOVE_GROUP, ps -> ps.setInt(1, gruppId)).close();
    }

    protected void lisaGrupiLiige(int gruppId, int liigeId) throws SQLException {
        executeUpdate(SQLStatements.ADD_GROUP_MEMBER, ps -> {
            ps.setInt(1, gruppId);
            ps.setInt(2, liigeId);
        }).close();
    }

    protected void kustutaGrupiLiige(int gruppId, int liigeId) throws SQLException {
        executeUpdate(SQLStatements.REMOVE_GROUP_MEMBER, ps -> {
            ps.setInt(1, gruppId);
            ps.setInt(2, liigeId);
        }).close();
    }

    protected int lisaKasutaja(String nimi) throws SQLException {
        ResultSet rs = executeUpdate(SQLStatements.ADD_USER, ps -> {
            ps.setString(1, nimi);
        });
        int saadus = rs.next() ? rs.getInt(1) : -1;
        rs.close();
        return saadus;
    }

    protected void kustutaKasutaja(int kasutajaId) throws SQLException {
        executeUpdate(SQLStatements.REMOVE_USER, ps -> ps.setInt(1, kasutajaId)).close();
    }

    protected Kasutaja leiaKasutaja(int kasutajaId) throws SQLException {
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

    protected Grupp leiaGrupp(int gruppId) throws SQLException {
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

    protected List<Integer> leiaGrupiLiikmed(int gruppId) throws SQLException {
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

    protected List<Integer> leiaKasutajaGrupid(int kasutajaId) throws SQLException {
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
                        result.add(new CalendarEvent(id, nimi, kirjeldus, algushetk, lopphetk, null));
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
                        result.add(new CalendarEvent(id, nimi, kirjeldus, algushetk, lopphetk, null));
                    }
                    return result;
                });
    }

    public List<CalendarEvent> leiaKõikSündmused() throws SQLException {
        return executeQuery(SQLStatements.GET_ALL_EVENTS,
                (_) -> {},
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
                        result.add(new CalendarEvent(id, nimi, kirjeldus, algushetk, lopphetk, null));
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
                        return new CalendarEvent(id, nimi, kirjeldus, algushetk, lopphetk, null);
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
     * Commitimine ei tundu siin väga vajalik... teeb lihtsalt asja raskemaks. Järgmises analoogses
     * meetodis ei kasuta.
     *
     * @param sql             SQL "skript"
     * @param parameterSetter consumer mis seab ps argumendid õigeks
     * @throws SQLException kui midagi valesti.
     */
    private ResultSet executeUpdate(String sql, SQLConsumer<PreparedStatement> parameterSetter) throws SQLException {
        Connection conn = DriverManager.getConnection(url);
        ResultSet rs;
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                parameterSetter.accept(ps);
                ps.executeUpdate();
                try {
                    rs = ps.getGeneratedKeys();
                } catch (Exception e) {
                    System.out.printf("See ei tohiks väga juhtuda tegelt");
                    throw e;
                }
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
        return rs;
    }

    /**
     * Analoogne eelnevale meetodile, kasutab consumerit, et parameetreid õigeks seada,
     * aga ei returni mitte ResultSet'i, vaid mapperit, mis teeb ResultSetiga tegevused ära
     * ning siis sulgeb selle (try-with resources'iga)
     *
     * @param sql             SQL "skript"
     * @param parameterSetter consumer mis seab ps argumendid õigeks
     * @param resultMapper    funktsioon mis kaardistab ResultSeti tulemuse
     * @throws SQLException kui midagi valesti.
     */
    private <T> T executeQuery(String sql, SQLConsumer<PreparedStatement> parameterSetter,
            SQLFunction<ResultSet, T> resultMapper) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url);
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

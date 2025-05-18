package oop.tegevusteplaneerija.common.util;

import java.sql.*;
import java.time.ZonedDateTime;

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

    protected ResultSet leiaKasutaja(int kasutajaId) throws SQLException {
        return executeUpdate(SQLStatements.GET_USER, ps -> ps.setInt(1, kasutajaId));
    }

    protected ResultSet leiaGrupp(int gruppId) throws SQLException {
        return executeUpdate(SQLStatements.GET_GROUP, ps -> ps.setInt(1, gruppId));
    }

    protected ResultSet leiaGrupiLiikmed(int gruppId) throws SQLException {
        return executeUpdate(SQLStatements.GET_GROUP_MEMBERS, ps -> ps.setInt(1, gruppId));
    }

    protected ResultSet leiaKasutajaGrupid(int kasutajaId) throws SQLException {
        return executeUpdate(SQLStatements.GET_USER_GROUPS, ps -> ps.setInt(1, kasutajaId));
    }

    protected ResultSet leiaGrupiSündmused(int gruppId) throws SQLException {
        return executeUpdate(SQLStatements.GET_GROUP_EVENTS, ps -> ps.setInt(1, gruppId));
    }

    protected ResultSet leiaKõikSündmused() throws SQLException {
        Connection conn = DriverManager.getConnection(url);
        PreparedStatement ps = conn.prepareStatement(SQLStatements.GET_ALL_EVENTS);
        return ps.executeQuery();
    }

    /**
     * Funktsioon, mis jooksutab consumerit, et parameetreid sättida ning siis
     * executeb statemendi.
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
     * Funktsionaalne interface, mis võimaldab SQL päringute täitmist koos
     * parameetritega. Teeb koodi veidi lühemaks ning saan rakendada õpitud asju ;)
     */
    @FunctionalInterface
    private interface SQLConsumer<PreparedStatement> {
        void accept(PreparedStatement t) throws SQLException;
    }
}

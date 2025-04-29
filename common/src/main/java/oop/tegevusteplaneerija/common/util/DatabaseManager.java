package oop.tegevusteplaneerija.common.util;

import java.sql.*;
import java.time.ZonedDateTime;

class DatabaseManager {
    private final String url;

    /**
     * "Madala taseme" objekt, mis initsialiseerib ja suhtleb andmebaasiga.
     * TODO: seda klassi klassi peab wrappima, et oleks kasutusel Kasutaja ja Grupp jne klassid.
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

    protected void lisaEvent(String nimi, String kirjeldus, ZonedDateTime algushetk, ZonedDateTime lopphetk, int grupp) throws SQLException {
        executeUpdate(SQLStatements.ADD_EVENT, ps -> {
            ps.setString(1, nimi);
            ps.setString(2, kirjeldus);
            ps.setTimestamp(3, Timestamp.from(algushetk.toInstant()));
            ps.setTimestamp(4, Timestamp.from(lopphetk.toInstant()));
            ps.setInt(5, grupp);
        });
    }

    protected void kustutaEvent(int eventId) throws SQLException {
        executeUpdate(SQLStatements.REMOVE_EVENT, ps -> ps.setInt(1, eventId));
    }

    protected void lisaGrupp(String nimi) throws SQLException {
        executeUpdate(SQLStatements.ADD_GROUP, ps -> ps.setString(1, nimi));
    }

    protected void kustutaGrupp(int gruppId) throws SQLException {
        executeUpdate(SQLStatements.REMOVE_GROUP, ps -> ps.setInt(1, gruppId));
    }

    protected void lisaGrupiLiige(int gruppId, int liigeId) throws SQLException {
        executeUpdate(SQLStatements.ADD_GROUP_MEMBER, ps -> {
            ps.setInt(1, gruppId);
            ps.setInt(2, liigeId);
        });
    }

    protected void kustutaGrupiLiige(int gruppId, int liigeId) throws SQLException {
        executeUpdate(SQLStatements.REMOVE_GROUP_MEMBER, ps -> {
            ps.setInt(1, gruppId);
            ps.setInt(2, liigeId);
        });
    }

    protected void lisaKasutaja(String nimi, int pgrupp) throws SQLException {
        executeUpdate(SQLStatements.ADD_USER, ps -> {
            ps.setString(1, nimi);
            ps.setInt(2, pgrupp);
        });
    }

    protected void kustutaKasutaja(int kasutajaId) throws SQLException {
        executeUpdate(SQLStatements.REMOVE_USER, ps -> ps.setInt(1, kasutajaId));
    }

    /**
     * Funktsioon, mis jooksutab consumerit, et parameetreid sättida ning siis
     * executeb statemendi.
     *
     * @param sql             SQL "skript"
     * @param parameterSetter consumer mis seab ps argumendid õigeks
     * @throws
     */
    private void executeUpdate(String sql, SQLConsumer<PreparedStatement> parameterSetter) throws SQLException {
        Connection conn = DriverManager.getConnection(url);
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                parameterSetter.accept(ps);
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
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

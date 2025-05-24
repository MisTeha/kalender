package oop.tegevusteplaneerija.server.util;

import oop.tegevusteplaneerija.common.util.DatabaseManager;
import oop.tegevusteplaneerija.common.util.SQLStatements;

import java.sql.*;
import java.time.ZonedDateTime;

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

}

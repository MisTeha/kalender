package oop.tegevusteplaneerija.common.util;

import oop.tegevusteplaneerija.common.mudel.CalendarEvent;
import oop.tegevusteplaneerija.common.mudel.Grupp;
import oop.tegevusteplaneerija.common.mudel.Kasutaja;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZonedDateTime;
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
    public abstract int lisaSündmus(String nimi, String kirjeldus, ZonedDateTime algushetk, ZonedDateTime lopphetk, int grupp) throws SQLException;
    public abstract void kustutaSündmus(int eventId) throws SQLException;
    public abstract void uuendaSündmus(int id, String nimi, String kirjeldus, ZonedDateTime algushetk, ZonedDateTime lopphetk, int grupp) throws SQLException;
    public abstract int lisaGrupp(String nimi, int omanikId, boolean personal) throws SQLException;
    public abstract void kustutaGrupp(int gruppId) throws SQLException;
    public abstract void lisaGrupiLiige(int gruppId, int liigeId) throws SQLException;
    public abstract void kustutaGrupiLiige(int gruppId, int liigeId) throws SQLException;
    public abstract int lisaKasutaja(String nimi) throws SQLException;
    public abstract void kustutaKasutaja(int kasutajaId) throws SQLException;
    public abstract Kasutaja leiaKasutaja(int kasutajaId) throws SQLException;
    public abstract Kasutaja leiaKasutaja(String nimi) throws SQLException;
    public abstract Grupp leiaGrupp(int gruppId) throws SQLException;
    public abstract List<Integer> leiaGrupiLiikmed(int gruppId) throws SQLException;
    public abstract List<Integer> leiaKasutajaGrupid(int kasutajaId) throws SQLException;
    public abstract List<CalendarEvent> leiaGrupiSündmused(int gruppId) throws SQLException;
    public abstract List<CalendarEvent> leiaKasutajaSündmused(int kasutajaId) throws SQLException;
    public abstract List<CalendarEvent> leiaKõikSündmused() throws SQLException;
    public abstract CalendarEvent leiaSündmus(int eventId) throws SQLException;
    public abstract Grupp leiaPersonaalneGrupp(int omanikId) throws SQLException;
}

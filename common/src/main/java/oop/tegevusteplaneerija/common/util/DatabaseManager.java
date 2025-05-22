package oop.tegevusteplaneerija.common.util;

import oop.tegevusteplaneerija.common.mudel.CalendarEvent;
import oop.tegevusteplaneerija.common.mudel.Grupp;
import oop.tegevusteplaneerija.common.mudel.Kasutaja;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Database manager'i interface smailifeis
 */
public interface DatabaseManager {
    void init() throws SQLException;
    int lisaEvent(String nimi, String kirjeldus, ZonedDateTime algushetk, ZonedDateTime lopphetk, int grupp) throws SQLException;
    void kustutaEvent(int eventId) throws SQLException;
    void uuendaSündmus(int id, String nimi, String kirjeldus, ZonedDateTime algushetk, ZonedDateTime lopphetk, int grupp) throws SQLException;
    int lisaGrupp(String nimi, int omanikId, boolean personal) throws SQLException;
    void kustutaGrupp(int gruppId) throws SQLException;
    void lisaGrupiLiige(int gruppId, int liigeId) throws SQLException;
    void kustutaGrupiLiige(int gruppId, int liigeId) throws SQLException;
    int lisaKasutaja(String nimi) throws SQLException;
    void kustutaKasutaja(int kasutajaId) throws SQLException;
    Kasutaja leiaKasutaja(int kasutajaId) throws SQLException;
    Kasutaja leiaKasutaja(String nimi) throws SQLException;
    Grupp leiaGrupp(int gruppId) throws SQLException;
    List<Integer> leiaGrupiLiikmed(int gruppId) throws SQLException;
    List<Integer> leiaKasutajaGrupid(int kasutajaId) throws SQLException;
    List<CalendarEvent> leiaGrupiSündmused(int gruppId) throws SQLException;
    List<CalendarEvent> leiaKasutajaSündmused(int kasutajaId) throws SQLException;
    List<CalendarEvent> leiaKõikSündmused() throws SQLException;
    CalendarEvent leiaSündmus(int eventId) throws SQLException;
    Grupp leiaPersonaalneGrupp(int omanikId) throws SQLException;
}

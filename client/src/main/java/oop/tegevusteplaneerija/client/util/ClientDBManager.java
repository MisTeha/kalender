package oop.tegevusteplaneerija.client.util;

import oop.tegevusteplaneerija.common.mudel.CalendarEvent;
import oop.tegevusteplaneerija.common.mudel.Grupp;
import oop.tegevusteplaneerija.common.mudel.Kasutaja;
import oop.tegevusteplaneerija.common.util.DatabaseManager;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Siia klassi tulevad eraldi SQL statementid ja kood, mis lihtsalt kustutab kliendi andmebaasi
 * andmed ära ning küsib serverilt uued.
 * Lisaks tuleb siia järgnev loogika andmete uuendamisel:
 * grupi/sündmuse/kasutaja/grupiliikme lisamisel või kustutamisel kõigepealt saadetakse request serveri
 * endpointile ning siis refreshitakse terve kliendi andmebaas serveri andmetega.
 *
 */
public class ClientDBManager implements DatabaseManager {

    private void clearKasutajad() {

    }

    private void clearGrupid() {

    }

    private void clearGrupiliikmed() {

    }

    private void clearSündmused() {

    }

    @Override
    public void init() throws SQLException {

    }

    @Override
    public int lisaSündmus(String nimi, String kirjeldus, ZonedDateTime algushetk, ZonedDateTime lopphetk, int grupp) throws SQLException {
        return 0;
    }

    @Override
    public void kustutaSündmus(int eventId) throws SQLException {

    }

    @Override
    public void uuendaSündmus(int id, String nimi, String kirjeldus, ZonedDateTime algushetk, ZonedDateTime lopphetk, int grupp) throws SQLException {

    }

    @Override
    public int lisaGrupp(String nimi, int omanikId, boolean personal) throws SQLException {
        return 0;
    }

    @Override
    public void kustutaGrupp(int gruppId) throws SQLException {

    }

    @Override
    public void lisaGrupiLiige(int gruppId, int liigeId) throws SQLException {

    }

    @Override
    public void kustutaGrupiLiige(int gruppId, int liigeId) throws SQLException {

    }

    @Override
    public int lisaKasutaja(String nimi) throws SQLException {
        return 0;
    }

    @Override
    public void kustutaKasutaja(int kasutajaId) throws SQLException {

    }

    @Override
    public Kasutaja leiaKasutaja(int kasutajaId) throws SQLException {
        return null;
    }

    @Override
    public Kasutaja leiaKasutaja(String nimi) throws SQLException {
        return null;
    }

    @Override
    public Grupp leiaGrupp(int gruppId) throws SQLException {
        return null;
    }

    @Override
    public List<Integer> leiaGrupiLiikmed(int gruppId) throws SQLException {
        return List.of();
    }

    @Override
    public List<Integer> leiaKasutajaGrupid(int kasutajaId) throws SQLException {
        return List.of();
    }

    @Override
    public List<CalendarEvent> leiaGrupiSündmused(int gruppId) throws SQLException {
        return List.of();
    }

    @Override
    public List<CalendarEvent> leiaKasutajaSündmused(int kasutajaId) throws SQLException {
        return List.of();
    }

    @Override
    public List<CalendarEvent> leiaKõikSündmused() throws SQLException {
        return List.of();
    }

    @Override
    public CalendarEvent leiaSündmus(int eventId) throws SQLException {
        return null;
    }

    @Override
    public Grupp leiaPersonaalneGrupp(int omanikId) throws SQLException {
        return null;
    }
}

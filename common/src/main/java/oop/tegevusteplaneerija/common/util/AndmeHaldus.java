package oop.tegevusteplaneerija.common.util;

import oop.tegevusteplaneerija.common.mudel.CalendarEvent;
import oop.tegevusteplaneerija.common.mudel.Grupp;
import oop.tegevusteplaneerija.common.mudel.Kasutaja;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.ZonedDateTime;

public class AndmeHaldus {
    private final DatabaseManager dbManager;

    public AndmeHaldus(String dbFilePath) throws SQLException {
        this.dbManager = new DatabaseManager(dbFilePath);
        dbManager.init();
    }

    public int lisaSündmus(CalendarEvent event) throws SQLException {
        return dbManager.lisaEvent(
                event.getNimi(),
                event.getKirjeldus(),
                event.getAlgushetk(),
                event.getLopphetk(),
                event.getGrupp().getId());
    }

    public void kustutaSündmus(CalendarEvent event) throws SQLException {
        dbManager.kustutaEvent(event.getId());
    }

    public int lisaKasutaja(Kasutaja kasutaja) throws SQLException {
        return dbManager.lisaKasutaja(kasutaja.getNimi());
    }

    public void kustutaKasutaja(Kasutaja kasutaja) throws SQLException {
        dbManager.kustutaKasutaja(kasutaja.getId());
    }

    public int lisaGrupp(Grupp grupp) throws SQLException {
        return dbManager.lisaGrupp(grupp.getNimi(), grupp.getOmanik().getId(), grupp.isPersonal());
    }

    public void kustutaGrupp(Grupp grupp) throws SQLException {
        dbManager.kustutaGrupp(grupp.getId());
    }

    public void lisaGrupiLiige(Grupp grupp, Kasutaja kasutaja) throws SQLException {
        dbManager.lisaGrupiLiige(grupp.getId(), kasutaja.getId());
    }

    public void kustutaGrupiLiige(Grupp grupp, Kasutaja kasutaja) throws SQLException {
        dbManager.kustutaGrupiLiige(grupp.getId(), kasutaja.getId());
    }

    public Kasutaja leiaKasutaja(int id) {
        try (ResultSet rs = dbManager.leiaKasutaja(id)) {
            rs.next();
            String nimi = rs.getString(1);
            return new Kasutaja(id, nimi, null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Grupp leiaGrupp(int id) {
        try (ResultSet rs = dbManager.leiaGrupp(id)) {
            rs.next();
            String nimi = rs.getString("nimi");
            int omanikuId = rs.getInt("omanik");
            Kasutaja omanik = leiaKasutaja(omanikuId);
            boolean personal = rs.getBoolean("personal");
            List<Kasutaja> liikmed = leiaGrupiLiikmed(id);
            Grupp grupp = new Grupp(id, nimi, omanik, liikmed, personal);
            return grupp;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Kasutaja> leiaGrupiLiikmed(int id) {
        try (ResultSet rs = dbManager.leiaGrupiLiikmed(id)) {
            List<Kasutaja> liikmed = new ArrayList<>();
            while (rs.next()) {
                Kasutaja liige = leiaKasutaja(rs.getInt(1));
                liikmed.add(liige);
            }
            return liikmed;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Grupp> leiaKasutajaGrupid(int kasutajaId) {
        try (ResultSet rs = dbManager.leiaKasutajaGrupid(kasutajaId)) {
            List<Grupp> grupid = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String nimi = rs.getString("nimi");
                int omanikId = rs.getInt("omanik");
                Kasutaja omanik = leiaKasutaja(omanikId);
                boolean personal = rs.getBoolean("personal");
                List<Kasutaja> liikmed = leiaGrupiLiikmed(id);
                grupid.add(new Grupp(id, nimi, omanik, liikmed, personal));
            }
            return grupid;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<CalendarEvent> leiaGrupiSündmused(int gruppId) {
        List<CalendarEvent> events = new ArrayList<>();
        try (ResultSet rs = dbManager.leiaGrupiSündmused(gruppId)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String nimi = rs.getString("nimi");
                String kirjeldus = rs.getString("kirjeldus");
                java.sql.Timestamp algus = rs.getTimestamp("algushetk");
                java.sql.Timestamp lopp = rs.getTimestamp("lopphetk");
                ZonedDateTime algushetk = algus.toInstant().atZone(java.time.ZoneId.systemDefault());
                ZonedDateTime lopphetk = lopp.toInstant().atZone(java.time.ZoneId.systemDefault());
                Grupp grupp = leiaGrupp(gruppId);
                events.add(new CalendarEvent(id, nimi, kirjeldus, algushetk, lopphetk, grupp));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return events;
    }

    public Map<Grupp, List<CalendarEvent>> leiaKasutajaGrupidJaSündmused(int kasutajaId) {
        Map<Grupp, List<CalendarEvent>> result = new HashMap<>();
        List<Grupp> grupid = leiaKasutajaGrupid(kasutajaId);
        for (Grupp grupp : grupid) {
            List<CalendarEvent> events = leiaGrupiSündmused(grupp.getId());
            result.put(grupp, events);
        }
        return result;
    }

    public List<CalendarEvent> leiaKõikSündmused() throws SQLException {
        List<CalendarEvent> events = new ArrayList<>();
        try (ResultSet rs = dbManager.leiaKõikSündmused()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String nimi = rs.getString("nimi");
                String kirjeldus = rs.getString("kirjeldus");
                java.sql.Timestamp algus = rs.getTimestamp("algushetk");
                java.sql.Timestamp lopp = rs.getTimestamp("lopphetk");
                int gruppId = rs.getInt("grupp");
                ZonedDateTime algushetk = algus.toInstant().atZone(java.time.ZoneId.systemDefault());
                ZonedDateTime lopphetk = lopp.toInstant().atZone(java.time.ZoneId.systemDefault());
                Grupp grupp = leiaGrupp(gruppId);
                events.add(new CalendarEvent(id, nimi, kirjeldus, algushetk, lopphetk, grupp));
            }
        }
        return events;
    }

    // Update event
    public void uuendaSündmus(CalendarEvent event) throws SQLException {
        throw new UnsupportedOperationException("uuendaSündmus is not yet implemented in AndmeHaldus");
    }

    // Find event by ID
    public CalendarEvent leiaSündmus(int id) {
        throw new UnsupportedOperationException("leiaSündmus is not yet implemented in AndmeHaldus");
    }

    // Find all events for a user
    public List<CalendarEvent> leiaKasutajaSündmused(int kasutajaId) {
        throw new UnsupportedOperationException("leiaKasutajaSündmused is not yet implemented in AndmeHaldus");
    }

    // Update user
    public void uuendaKasutaja(Kasutaja kasutaja) throws SQLException {
        throw new UnsupportedOperationException("uuendaKasutaja is not yet implemented in AndmeHaldus");
    }
}

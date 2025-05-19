package oop.tegevusteplaneerija.common.util;

import oop.tegevusteplaneerija.common.mudel.CalendarEvent;
import oop.tegevusteplaneerija.common.mudel.Grupp;
import oop.tegevusteplaneerija.common.mudel.Kasutaja;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AndmeHaldus {
    private final DatabaseManager dbManager;

    public AndmeHaldus(String dbFilePath) throws SQLException {
        this.dbManager = new DatabaseManager(dbFilePath);
        dbManager.init();
    }

    public int lisaSündmus(CalendarEvent event) throws SQLException {
        int id = dbManager.lisaEvent(
                event.getNimi(),
                event.getKirjeldus(),
                event.getAlgushetk(),
                event.getLopphetk(),
                event.getGrupp().getId());
        event.setId(id);
        return id;
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
        try {
            return dbManager.leiaKasutaja(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Grupp leiaGrupp(int id) {
        try {
            Grupp grupp = dbManager.leiaGrupp(id);
            if (grupp == null)
                return null;
            Kasutaja omanik = leiaKasutaja(grupp.getOmanik().getId());
            List<Kasutaja> liikmed = leiaGrupiLiikmed(id);
            return new Grupp(grupp.getId(), grupp.getNimi(), omanik, liikmed, grupp.isPersonal());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Kasutaja> leiaGrupiLiikmed(int id) {
        try {
            List<Integer> liikmeIds = dbManager.leiaGrupiLiikmed(id);
            List<Kasutaja> liikmed = new ArrayList<>();
            for (int liikmeId : liikmeIds) {
                Kasutaja liige = leiaKasutaja(liikmeId);
                if (liige != null)
                    liikmed.add(liige);
            }
            return liikmed;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Grupp> leiaKasutajaGrupid(int kasutajaId) {
        try {
            List<Integer> gruppIds = dbManager.leiaKasutajaGrupid(kasutajaId);
            List<Grupp> grupid = new ArrayList<>();
            for (int gruppId : gruppIds) {
                Grupp grupp = leiaGrupp(gruppId);
                if (grupp != null)
                    grupid.add(grupp);
            }
            return grupid;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<CalendarEvent> leiaGrupiSündmused(int gruppId) {
        try {
            return dbManager.leiaGrupiSündmused(gruppId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<CalendarEvent> leiaKõikSündmused() throws SQLException {
        return dbManager.leiaKõikSündmused();
    }

    public CalendarEvent leiaSündmus(int eventId) {
        try {
            return dbManager.leiaSündmus(eventId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // pole praegu kasutuses, tuleviku jaoks
    public List<CalendarEvent> leiaKasutajaSündmused(int kasutajaId) {
        try {
            return dbManager.leiaKasutajaSündmused(kasutajaId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Grupp leiaPersonaalneGrupp(int omanikId) {
        try {
            return dbManager.leiaPersonaalneGrupp(omanikId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
}

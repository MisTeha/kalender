package oop.tegevusteplaneerija.common.teenused;

import oop.tegevusteplaneerija.common.mudel.CalendarEvent;
import oop.tegevusteplaneerija.common.util.AndmeHaldus;

import java.sql.SQLException;
import java.util.List;

public class EventTeenus {
    private final AndmeHaldus andmeHaldus;

    public EventTeenus(String dbFilePath) throws SQLException {
        this.andmeHaldus = new AndmeHaldus(dbFilePath);
    }

    public int lisaSündmus(CalendarEvent event) throws SQLException {
        return andmeHaldus.lisaSündmus(event);
    }

    public void kustutaSündmus(CalendarEvent event) throws SQLException {
        andmeHaldus.kustutaSündmus(event);
    }

    public List<CalendarEvent> leiaKõikSündmused() throws SQLException {
        return andmeHaldus.leiaKõikSündmused();
    }

    public List<CalendarEvent> leiaGrupiSündmused(int gruppId) {
        return andmeHaldus.leiaGrupiSündmused(gruppId);
    }

    public void uuendaSündmus(CalendarEvent event) throws SQLException {
        andmeHaldus.uuendaSündmus(event);
    }

    public CalendarEvent leiaSündmus(int id) {
        return andmeHaldus.leiaSündmus(id);
    }

    public List<CalendarEvent> leiaKasutajaSündmused(int kasutajaId) {
        return andmeHaldus.leiaKasutajaSündmused(kasutajaId);
    }
}

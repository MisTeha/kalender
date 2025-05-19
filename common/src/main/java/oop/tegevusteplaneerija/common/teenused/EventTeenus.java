package oop.tegevusteplaneerija.common.teenused;

import oop.tegevusteplaneerija.common.mudel.CalendarEvent;
import oop.tegevusteplaneerija.common.util.AndmeHaldus;

import java.sql.SQLException;
import java.util.List;

public class EventTeenus {
    private final AndmeHaldus andmeHaldus;

    public EventTeenus(AndmeHaldus andmeHaldus) throws SQLException {
        this.andmeHaldus = andmeHaldus;
    }

    public CalendarEvent lisaSündmus(CalendarEvent event) throws SQLException {
        int id = andmeHaldus.lisaSündmus(event);
        event.setId(id);
        return event;
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

    public CalendarEvent leiaSündmus(int eventId) {
        return andmeHaldus.leiaSündmus(eventId);
    }
}

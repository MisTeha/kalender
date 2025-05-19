package oop.tegevusteplaneerija.common.teenused;

import oop.tegevusteplaneerija.common.mudel.CalendarEvent;
import oop.tegevusteplaneerija.common.mudel.Grupp;
import oop.tegevusteplaneerija.common.mudel.Kasutaja;
import oop.tegevusteplaneerija.common.util.AndmeHaldus;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class KasutajaTeenus {
    private final AndmeHaldus andmeHaldus;
    private final GrupiTeenus grupiTeenus;

    public KasutajaTeenus(AndmeHaldus andmeHaldus) {
        this.andmeHaldus = andmeHaldus;
        this.grupiTeenus = new GrupiTeenus(andmeHaldus);
    }

    public Kasutaja looKasutaja(String nimi) throws SQLException {
        Kasutaja kasutaja = new Kasutaja(nimi, null);
        int kasId = andmeHaldus.lisaKasutaja(kasutaja);
        kasutaja.setId(kasId);
        Grupp grupp = grupiTeenus.looPersonaalneGrupp(nimi + "-isiklik", kasutaja);
        kasutaja.setPersonalGrupp(grupp);
        return kasutaja;
    }

    public Kasutaja leiaKasutaja(int id) {
        return andmeHaldus.leiaKasutaja(id);
    }

    public List<Grupp> leiaKasutajaGrupid(Kasutaja kasutaja) {
        return andmeHaldus.leiaKasutajaGrupid(kasutaja.getId());
    }

    public Map<Grupp, List<CalendarEvent>> leiaKasutajaSündmused(Kasutaja kasutaja) {
        return andmeHaldus.leiaKasutajaGrupidJaSündmused(kasutaja.getId());
    }

    public void kustutaKasutaja(Kasutaja kasutaja) throws SQLException {
        andmeHaldus.kustutaKasutaja(kasutaja);
    }

    public void lisaPersonaalneGrupp(Kasutaja kasutaja) {
        try {
            Grupp personal = grupiTeenus.leiaPersonaalneGrupp(kasutaja);
            if (personal != null) {
                kasutaja.setPersonalGrupp(personal);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
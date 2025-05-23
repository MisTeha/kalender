package oop.tegevusteplaneerija.common.teenused;

import oop.tegevusteplaneerija.common.mudel.Grupp;
import oop.tegevusteplaneerija.common.mudel.Kasutaja;
import oop.tegevusteplaneerija.common.util.AndmeHaldus;

import java.sql.SQLException;
import java.util.List;

public class GrupiTeenus {
    private final AndmeHaldus andmeHaldus;

    public GrupiTeenus(AndmeHaldus andmeHaldus) {
        this.andmeHaldus = andmeHaldus;
    }

    public Grupp leiaGrupp(int id) {
        return andmeHaldus.leiaGrupp(id);
    }

    public Grupp looKoostööGrupp(String nimi, Kasutaja omanik, List<Kasutaja> liikmed) throws SQLException {
        Grupp grupp = new Grupp(nimi, omanik, liikmed, false);
        int id = andmeHaldus.lisaGrupp(grupp);
        grupp.setId(id);
        andmeHaldus.lisaGrupiLiige(grupp, omanik);
        for (Kasutaja kasutaja : liikmed) {
            lisaGrupiLiige(grupp, kasutaja);
        }
        return grupp;
    }

    public Grupp looPersonaalneGrupp(String nimi, Kasutaja omanik) throws SQLException {
        Grupp existing = andmeHaldus.leiaPersonaalneGrupp(omanik.getId());
        if (existing != null) {
            return existing;
        }
        Grupp grupp = new Grupp(nimi, omanik, List.of(), true);
        int id = andmeHaldus.lisaGrupp(grupp);
        grupp.setId(id);
        andmeHaldus.lisaGrupiLiige(grupp, omanik);
        return grupp;
    }

    public void kustutaGrupp(Grupp grupp) throws SQLException {
        andmeHaldus.kustutaGrupp(grupp);
    }

    public void lisaGrupiLiige(Grupp grupp, Kasutaja kasutaja) throws SQLException {
        andmeHaldus.lisaGrupiLiige(grupp, kasutaja);
    }

    public void kustutaGrupiLiige(Grupp grupp, Kasutaja kasutaja) throws SQLException {
        andmeHaldus.kustutaGrupiLiige(grupp, kasutaja);
    }

    public List<Kasutaja> leiaGrupiLiikmed(int gruppId) {
        return andmeHaldus.leiaGrupiLiikmed(gruppId);
    }

    public List<Grupp> leiaKasutajaGrupid(int kasutajaId) {
        return andmeHaldus.leiaKasutajaGrupid(kasutajaId);
    }

    public Grupp leiaPersonaalneGrupp(Kasutaja omanik) {
        return andmeHaldus.leiaPersonaalneGrupp(omanik.getId());
    }
}
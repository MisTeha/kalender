package oop.tegevusteplaneerija.common.mudel;

import java.time.ZonedDateTime;

public class CalendarEvent {
    private int id;
    private String nimi;
    private String kirjeldus;
    private ZonedDateTime algushetk;
    private ZonedDateTime lopphetk;
    private Grupp grupp;

    public CalendarEvent(int id, String nimi, String kirjeldus, ZonedDateTime algushetk, ZonedDateTime lopphetk,
            Grupp grupp) {
        this.id = id;
        this.nimi = nimi;
        this.kirjeldus = kirjeldus;
        this.algushetk = algushetk;
        this.lopphetk = lopphetk;
        this.grupp = grupp;
    }

    public CalendarEvent(String nimi, String kirjeldus, ZonedDateTime algushetk, ZonedDateTime lopphetk, Grupp grupp) {
        this(-1, nimi, kirjeldus, algushetk, lopphetk, grupp);
    }

    // Getters
    public String getNimi() {
        return nimi;
    }

    public int getId() {
        return id;
    }

    public String getKirjeldus() {
        return kirjeldus;
    }

    public ZonedDateTime getAlgushetk() {
        return algushetk;
    }

    public ZonedDateTime getLopphetk() {
        return lopphetk;
    }

    public Grupp getGrupp() {
        return grupp;
    }

    @Override
    public String toString() {
        return "CalendarEvent{" +
                "nimi='" + nimi + '\'' +
                ", kirjeldus='" + kirjeldus + '\'' +
                ", algushetk=" + algushetk +
                ", lopphetk=" + lopphetk +
                ", grupp=" + (grupp != null ? grupp.getId() : "null") +
                '}';
    }
}

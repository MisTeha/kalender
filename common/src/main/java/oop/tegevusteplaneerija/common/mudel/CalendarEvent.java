package oop.tegevusteplaneerija.common.mudel;

import java.time.ZonedDateTime;

public class CalendarEvent {
    private Integer id;
    private String nimi;
    private String kirjeldus;
    private ZonedDateTime algushetk;
    private ZonedDateTime lopphetk;
    private Grupp grupp;

    public CalendarEvent(Integer id, String nimi, String kirjeldus, ZonedDateTime algushetk, ZonedDateTime lopphetk,
            Grupp grupp) {
        this.id = id;
        this.nimi = nimi;
        this.kirjeldus = kirjeldus;
        this.algushetk = algushetk;
        this.lopphetk = lopphetk;
        this.grupp = grupp;
    }

    public CalendarEvent(String nimi, String kirjeldus, ZonedDateTime algushetk, ZonedDateTime lopphetk, Grupp grupp) {
        this(null, nimi, kirjeldus, algushetk, lopphetk, grupp);
    }

    // Getters
    public String getNimi() {
        return nimi;
    }

    public int getId() {
        return id != null ? id : -1;
    }

    public void setId(int id) {
        this.id = id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CalendarEvent that = (CalendarEvent) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

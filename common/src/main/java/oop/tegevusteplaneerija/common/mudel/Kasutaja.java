package oop.tegevusteplaneerija.common.mudel;


public class Kasutaja {
    private Integer id;
    private String nimi;
    private Grupp personalGrupp;



    public Kasutaja(Integer id, String nimi, Grupp personalGrupp) {
        this.id = id;
        this.nimi = nimi;
        this.personalGrupp = personalGrupp;
    }

    public Kasutaja(String nimi, Grupp personalGrupp) {
        this(null, nimi, personalGrupp);
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }

    public int getId() {
        return id;
    }

    public String getNimi() {
        return nimi;
    }

    public Grupp getPersonalGrupp() {
        return personalGrupp;
    }

    public void setPersonalGrupp(Grupp personalGrupp) {
        this.personalGrupp = personalGrupp;
    }
}

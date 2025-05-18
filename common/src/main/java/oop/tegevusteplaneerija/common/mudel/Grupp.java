package oop.tegevusteplaneerija.common.mudel;

import java.util.ArrayList;
import java.util.List;

public class Grupp {
    private String nimi;
    private Integer id; //null enne andmebaasi lisamist
    private final List<Kasutaja> liikmed;
    private final Kasutaja omanik;
    private final boolean personal;

    public Grupp(Integer id, String nimi, Kasutaja omanik, List<Kasutaja> liikmed, boolean personal) {
        this.nimi = nimi;
        this.liikmed = liikmed;
        this.id = id;
        this.omanik = omanik;
        this.personal = personal;
    }

    public Grupp(String nimi, Kasutaja omanik, List<Kasutaja> liikmed, boolean personal) {
        this(null, nimi, omanik, liikmed, personal);
    }

    public Grupp(String nimi, Kasutaja omanik, boolean personal) {
        this(nimi, omanik, new ArrayList<>(), personal);
    }


    /**
     * Tagastab <i>immutable</i> listi liikmetest.
     *
     * @return List liikmetest
     */
    public List<Kasutaja> getLiikmed() {
        return List.copyOf(liikmed);
    }

    public Kasutaja getOmanik() {
        return omanik;
    }

    public int getId() {
        return id;
    }

    public String getNimi() {
        return nimi;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }

    public boolean isPersonal() {
        return personal;
    }
}

package com.project;

import java.io.Serializable;

/**
 * Ciutada: costat "Many" de la relació 1:N amb Ciutat.
 * És el costat propietari: la seva taula té la clau forana ciutatId.
 * El mapeig amb la taula es defineix a Ciutada.hbm.xml.
 */
public class Ciutada implements Serializable {

    private static final long serialVersionUID = 1L;

    private long ciutadaId;
    private String nom;
    private String cognom;
    private int edat;

    // Ciutat a la qual pertany (pot ser null)
    private Ciutat ciutat;

    // Constructor buit: obligatori per a Hibernate
    public Ciutada() {}

    public Ciutada(String nom, String cognom, int edat) {
        this.nom = nom;
        this.cognom = cognom;
        this.edat = edat;
    }

    public long getCiutadaId() { return ciutadaId; }
    public void setCiutadaId(long ciutadaId) { this.ciutadaId = ciutadaId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getCognom() { return cognom; }
    public void setCognom(String cognom) { this.cognom = cognom; }

    public int getEdat() { return edat; }
    public void setEdat(int edat) { this.edat = edat; }

    public Ciutat getCiutat() { return ciutat; }
    public void setCiutat(Ciutat ciutat) { this.ciutat = ciutat; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ciutada other)) return false;
        return ciutadaId != 0 && ciutadaId == other.getCiutadaId();
    }

    @Override
    public int hashCode() {
        return Ciutada.class.hashCode();
    }

    // No inclou la ciutat: evita recursió infinita i càrregues lazy inesperades
    @Override
    public String toString() {
        return ciutadaId + ": " + nom + " " + cognom + " (" + edat + " anys)";
    }
}

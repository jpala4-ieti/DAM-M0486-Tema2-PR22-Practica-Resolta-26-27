package com.project;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Ciutada: costat "Many" de la relació 1:N amb Ciutat.
 * És el costat propietari: la seva taula té la clau forana ciutatId.
 * El mapeig amb la taula es defineix amb anotacions JPA sobre els atributs.
 */
@Entity
@Table(name = "Ciutada")
public class Ciutada implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ciutadaId")
    private long ciutadaId;

    @Column(name = "nom")
    private String nom;

    @Column(name = "cognom")
    private String cognom;

    @Column(name = "edat")
    private int edat;

    // Costat propietari: defineix la columna de la clau forana (pot ser null).
    // LAZY: la ciutat només es carrega si s'hi accedeix (@ManyToOne és EAGER per defecte).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ciutatId")
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

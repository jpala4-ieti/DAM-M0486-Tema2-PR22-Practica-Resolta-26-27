package com.project;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Ciutat: costat "One" de la relació 1:N amb Ciutada.
 * El mapeig amb la taula es defineix amb anotacions JPA sobre els atributs.
 */
@Entity
@Table(name = "Ciutat")
public class Ciutat implements Serializable {

    private static final long serialVersionUID = 1L;

    // IDENTITY: la clau la genera la base de dades (autoincrement)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ciutatId")
    private long ciutatId;

    @Column(name = "nom")
    private String nom;

    @Column(name = "pais")
    private String pais;

    @Column(name = "poblacio")
    private int poblacio;

    // Costat invers (no propietari): la FK és a la taula Ciutada i la gestiona
    // l'atribut Ciutada.ciutat. Per defecte @OneToMany és LAZY.
    // Sense cascade: esborrar una ciutat no esborra els seus ciutadans.
    @OneToMany(mappedBy = "ciutat")
    private Set<Ciutada> ciutadans = new HashSet<>();

    // Constructor buit: obligatori per a Hibernate
    public Ciutat() {}

    public Ciutat(String nom, String pais, int poblacio) {
        this.nom = nom;
        this.pais = pais;
        this.poblacio = poblacio;
    }

    public long getCiutatId() { return ciutatId; }
    public void setCiutatId(long ciutatId) { this.ciutatId = ciutatId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public int getPoblacio() { return poblacio; }
    public void setPoblacio(int poblacio) { this.poblacio = poblacio; }

    public Set<Ciutada> getCiutadans() { return ciutadans; }
    // Des del codi de l'aplicació cal usar addCiutada / removeCiutada
    public void setCiutadans(Set<Ciutada> ciutadans) { this.ciutadans = ciutadans; }

    /** Afegeix un ciutadà i actualitza ELS DOS costats de la relació. */
    public void addCiutada(Ciutada ciutada) {
        ciutadans.add(ciutada);
        ciutada.setCiutat(this);
    }

    /** Treu un ciutadà i actualitza ELS DOS costats de la relació. */
    public void removeCiutada(Ciutada ciutada) {
        ciutadans.remove(ciutada);
        ciutada.setCiutat(null);
    }

    // Igualtat basada en l'identificador; hash constant perquè l'id
    // canvia quan l'objecte es persisteix.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ciutat other)) return false;
        return ciutatId != 0 && ciutatId == other.getCiutatId();
    }

    @Override
    public int hashCode() {
        return Ciutat.class.hashCode();
    }

    @Override
    public String toString() {
        String llista = ciutadans.stream()
                .sorted(Comparator.comparingLong(Ciutada::getCiutadaId))
                .map(c -> c.getNom() + " " + c.getCognom())
                .collect(Collectors.joining(" | "));
        return ciutatId + ": " + nom + " (" + pais + "), Població: " + poblacio
                + ", Ciutadans: [" + llista + "]";
    }
}

package com.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Exercici d'ampliació: comprovacions simples de les funcions del Manager.
 * Fa servir src/test/resources/hibernate.properties (./data/database-test.db).
 */
class CiutatCiutadaTest {

    @BeforeAll
    static void setUp() {
        new File("data").mkdirs();
        Manager.createSessionFactory();
    }

    @AfterAll
    static void tearDown() {
        Manager.close();
    }

    @Test
    @DisplayName("addCiutat assigna un identificador i desa les dades")
    void addCiutatAssignaIdentificador() {
        Ciutat ciutat = Manager.addCiutat("Girona", "Catalunya", 17001);

        assertTrue(ciutat.getCiutatId() > 0);
        Ciutat desada = Manager.getCiutatWithCiutadans(ciutat.getCiutatId());
        assertEquals("Girona", desada.getNom());
        assertTrue(desada.getCiutadans().isEmpty());
    }

    @Test
    @DisplayName("updateCiutat assigna i treu ciutadans")
    void updateCiutatAssignaElsCiutadans() {
        Ciutat ciutat = Manager.addCiutat("Reus", "Catalunya", 43201);
        Ciutada anna = Manager.addCiutada("Anna", "Puig", 30);
        Ciutada pere = Manager.addCiutada("Pere", "Soler", 41);

        Manager.updateCiutat(ciutat.getCiutatId(), "Reus", "Catalunya", 43201, Set.of(anna, pere));
        assertEquals(2, Manager.getCiutatWithCiutadans(ciutat.getCiutatId()).getCiutadans().size());

        // Amb un sol ciutadà, l'altre deixa de pertànyer a la ciutat
        Manager.updateCiutat(ciutat.getCiutatId(), "Reus", "Catalunya", 43201, Set.of(anna));
        assertEquals(1, Manager.getCiutatWithCiutadans(ciutat.getCiutatId()).getCiutadans().size());

        // Amb null, la relació no es toca
        Manager.updateCiutat(ciutat.getCiutatId(), "Reus Updated", "Catalunya", 43201, null);
        Ciutat desada = Manager.getCiutatWithCiutadans(ciutat.getCiutatId());
        assertEquals("Reus Updated", desada.getNom());
        assertEquals(1, desada.getCiutadans().size());
    }

    @Test
    @DisplayName("updateCiutada canvia les dades del ciutadà")
    void updateCiutadaCanviaLesDades() {
        Ciutada ciutada = Manager.addCiutada("Joan", "Mas", 50);

        Manager.updateCiutada(ciutada.getCiutadaId(), "Joan Updated", "Mas", 51);

        List<Ciutada> trobats = Manager.listCollection(Ciutada.class,
                "ciutadaId = " + ciutada.getCiutadaId());
        assertEquals(1, trobats.size());
        assertEquals("Joan Updated", trobats.get(0).getNom());
        assertEquals(51, trobats.get(0).getEdat());
    }

    @Test
    @DisplayName("delete d'una ciutat no esborra els seus ciutadans")
    void deleteCiutatNoEsborraElsCiutadans() {
        Ciutat ciutat = Manager.addCiutat("Vic", "Catalunya", 8500);
        Ciutada marta = Manager.addCiutada("Marta", "Vila", 25);
        Manager.updateCiutat(ciutat.getCiutatId(), "Vic", "Catalunya", 8500, Set.of(marta));

        Manager.delete(Ciutat.class, ciutat.getCiutatId());

        assertNull(Manager.getCiutatWithCiutadans(ciutat.getCiutatId()));
        assertNotNull(Manager.listCollection(Ciutada.class).stream()
                .filter(c -> c.getCiutadaId() == marta.getCiutadaId())
                .findFirst().orElse(null));
    }

    @Test
    @DisplayName("toString de Ciutat mostra els ciutadans")
    void toStringDeCiutat() {
        Ciutat ciutat = Manager.addCiutat("Olot", "Catalunya", 17800);
        Ciutada pau = Manager.addCiutada("Pau", "Roca", 33);
        Manager.updateCiutat(ciutat.getCiutatId(), "Olot", "Catalunya", 17800, Set.of(pau));

        Ciutat desada = Manager.getCiutatWithCiutadans(ciutat.getCiutatId());
        assertEquals(ciutat.getCiutatId() + ": Olot (Catalunya), Població: 17800, Ciutadans: [Pau Roca]",
                desada.toString());
    }
}

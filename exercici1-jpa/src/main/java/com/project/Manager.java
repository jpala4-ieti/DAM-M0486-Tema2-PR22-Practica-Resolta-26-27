package com.project;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

/**
 * Classe Manager: centralitza totes les operacions amb la base de dades
 * (patró DAO) perquè el Main no hagi de conèixer Hibernate.
 */
public class Manager {

    private static final String DEFAULT_PROPERTIES = "hibernate.properties";

    // Una sola SessionFactory per a tota l'aplicació (és cara de crear)
    private static SessionFactory factory;

    // Fitxer de propietats que farà servir createSessionFactory()
    private static String propertiesFile = DEFAULT_PROPERTIES;

    /**
     * Exercici d'ampliació: permet triar la base de dades abans de crear la
     * SessionFactory (per exemple "hibernate-mysql.properties").
     */
    public static void setPropertiesFile(String fileName) {
        propertiesFile = fileName;
    }

    public static void createSessionFactory() {
        createSessionFactory(propertiesFile);
    }

    /**
     * Crea la SessionFactory. S'ha de cridar un sol cop, a l'inici.
     * hibernate.properties es carrega automàticament del classpath; si se
     * n'indica un altre, les seves propietats substitueixen les anteriors.
     */
    public static void createSessionFactory(String propertiesFileName) {
        try {
            Configuration configuration = new Configuration();
            // Classes mapejades amb anotacions JPA (no calen hibernate.cfg.xml ni .hbm.xml)
            configuration.addAnnotatedClass(Ciutat.class);
            configuration.addAnnotatedClass(Ciutada.class);
            if (!DEFAULT_PROPERTIES.equals(propertiesFileName)) {
                Properties properties = new Properties();
                try (InputStream input = Manager.class.getClassLoader()
                        .getResourceAsStream(propertiesFileName)) {
                    if (input == null) {
                        throw new IOException("No s'ha pogut trobar " + propertiesFileName);
                    }
                    properties.load(input);
                }
                configuration.addProperties(properties);
            }

            factory = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Error en crear sessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static void close() {
        if (factory != null) {
            factory.close();
            factory = null;
        }
    }

    // ============================================================
    // GESTIÓ DE SESSIONS I TRANSACCIONS
    // ============================================================

    /**
     * Executa una acció dins d'una transacció. El try-with-resources tanca
     * sempre la sessió; si hi ha qualsevol error es fa rollback.
     */
    private static void executeInTransaction(Consumer<Session> action) {
        Transaction tx = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            action.accept(session);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RuntimeException("Error en transacció Hibernate", e);
        }
    }

    /** Com l'anterior, però retorna un resultat. */
    private static <T> T executeInTransactionWithResult(Function<Session, T> action) {
        Transaction tx = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            T result = action.apply(session);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RuntimeException("Error en transacció Hibernate", e);
        }
    }

    // ============================================================
    // CREATE
    // ============================================================

    public static Ciutat addCiutat(String nom, String pais, int poblacio) {
        return executeInTransactionWithResult(session -> {
            Ciutat ciutat = new Ciutat(nom, pais, poblacio);
            session.persist(ciutat);   // Hibernate li assigna l'identificador
            return ciutat;
        });
    }

    public static Ciutada addCiutada(String nom, String cognom, int edat) {
        return executeInTransactionWithResult(session -> {
            Ciutada ciutada = new Ciutada(nom, cognom, edat);
            session.persist(ciutada);
            return ciutada;
        });
    }

    // ============================================================
    // UPDATE
    // ============================================================

    /**
     * Actualitza les dades d'una ciutat i el conjunt dels seus ciutadans.
     * Si ciutadans és null, la relació existent no es toca.
     * No cal cridar merge: l'objecte recuperat amb find està gestionat per la
     * sessió i Hibernate desa els canvis en fer commit.
     */
    public static void updateCiutat(long ciutatId, String nom, String pais, int poblacio,
                                    Set<Ciutada> ciutadans) {
        executeInTransaction(session -> {
            Ciutat ciutat = session.find(Ciutat.class, ciutatId);
            if (ciutat == null) return;

            ciutat.setNom(nom);
            ciutat.setPais(pais);
            ciutat.setPoblacio(poblacio);

            if (ciutadans != null) {
                // 1. Treu els ciutadans que ja no pertanyen a la ciutat
                //    (còpia per evitar ConcurrentModificationException)
                for (Ciutada actual : new ArrayList<>(ciutat.getCiutadans())) {
                    if (!ciutadans.contains(actual)) {
                        ciutat.removeCiutada(actual);
                    }
                }
                // 2. Afegeix els nous, sempre amb l'objecte gestionat per la sessió
                for (Ciutada ciutada : ciutadans) {
                    Ciutada gestionat = session.find(Ciutada.class, ciutada.getCiutadaId());
                    if (gestionat != null) {
                        ciutat.addCiutada(gestionat);
                    }
                }
            }
        });
    }

    public static void updateCiutada(long ciutadaId, String nom, String cognom, int edat) {
        executeInTransaction(session -> {
            Ciutada ciutada = session.find(Ciutada.class, ciutadaId);
            if (ciutada == null) return;
            ciutada.setNom(nom);
            ciutada.setCognom(cognom);
            ciutada.setEdat(edat);
        });
    }

    // ============================================================
    // READ
    // ============================================================

    /**
     * Recupera una ciutat amb els seus ciutadans en una sola consulta
     * (JOIN FETCH), de manera que es poden llegir amb la sessió ja tancada.
     * Retorna null si la ciutat no existeix.
     */
    public static Ciutat getCiutatWithCiutadans(long ciutatId) {
        return executeInTransactionWithResult(session -> session.createQuery(
                "SELECT c FROM Ciutat c LEFT JOIN FETCH c.ciutadans WHERE c.ciutatId = :id",
                Ciutat.class)
                .setParameter("id", ciutatId)
                .getSingleResultOrNull());
    }

    /**
     * Llista tots els objectes d'una classe, opcionalment amb una condició HQL.
     * COMPTE: whereClause es concatena a la consulta; no hi poseu mai text
     * introduït per l'usuari (injecció). Per a això, paràmetres amb nom.
     */
    public static <T> List<T> listCollection(Class<T> clazz, String whereClause) {
        return executeInTransactionWithResult(session -> {
            String hql = "FROM " + clazz.getName();
            if (whereClause != null && !whereClause.trim().isEmpty()) {
                hql += " WHERE " + whereClause;
            }
            List<T> result = session.createQuery(hql, clazz).getResultList();
            // La col·lecció de ciutadans és LAZY: es carrega abans de tancar la
            // sessió perquè toString() no llanci LazyInitializationException
            for (T obj : result) {
                if (obj instanceof Ciutat ciutat) {
                    Hibernate.initialize(ciutat.getCiutadans());
                }
            }
            return result;
        });
    }

    public static <T> List<T> listCollection(Class<T> clazz) {
        return listCollection(clazz, "");
    }

    // ============================================================
    // DELETE
    // ============================================================

    public static <T> void delete(Class<T> clazz, Serializable id) {
        executeInTransaction(session -> {
            T obj = session.find(clazz, id);
            if (obj == null) return;
            if (obj instanceof Ciutat ciutat) {
                // Els ciutadans no s'esborren: es queden sense ciutat (FK a null)
                for (Ciutada ciutada : new ArrayList<>(ciutat.getCiutadans())) {
                    ciutat.removeCiutada(ciutada);
                }
            }
            session.remove(obj);
        });
    }

    // ============================================================
    // UTILITATS
    // ============================================================

    /** Converteix una col·lecció en text, un element per línia. */
    public static <T> String collectionToString(Class<? extends T> clazz, Collection<T> collection) {
        StringBuilder sb = new StringBuilder();
        for (T obj : collection) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(obj.toString());
        }
        return sb.toString();
    }
}

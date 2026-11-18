# PR2.2 Introducció a Hibernate — solució de referència (curs 26-27)

Solució feta sobre el repositori `DAM-M0486-Tema2-PR22-Punt-Partida-26-27`.
`Main.java` és el del punt de partida, sense cap canvi.

## Contingut

| Carpeta | Què hi ha |
|---|---|
| `exercici0-hbm/` | Exercici 0: mapeig amb `.hbm.xml` (`hibernate.cfg.xml`, `Ciutat.hbm.xml`, `Ciutada.hbm.xml`) |
| `exercici1-jpa/` | Exercici 1: el mateix programa amb anotacions JPA (`jakarta.persistence.*`) |
| `doc/` | Exercici 2: `memoria.pdf` amb els dos diagrames, i els diagrames en `.png` |

Els dos projectes comparteixen `Main.java`, `MainMenu.java`, el test i gairebé tot el
`Manager.java`. Canvien les classes `Ciutat` i `Ciutada` (amb o sense anotacions) i la
manera de registrar el mapeig a `Manager.createSessionFactory`.

## Execució

Des de la carpeta de cada exercici (Java 21 i Maven):

```
./run.sh com.project.Main        # Linux/macOS   (.\run.ps1 com.project.Main a Windows)
./run.sh com.project.MainMenu    # ampliació: menú per triar SQLite o MySQL
mvn test                         # ampliació: tests del Manager
```

Per a MySQL cal engegar abans el contenidor de la carpeta `docker/` (`docker-compose up -d`).

A l'exercici 0, Hibernate mostra a l'arrencada un avís de deprecació pels `.hbm.xml`.
És l'esperat: el format està obsolet des de Hibernate 6 i s'eliminarà a Hibernate 8.

## Decisions de disseny

- **Relació 1:N bidireccional.** `Ciutada` és el costat propietari (té la columna
  `ciutatId`); `Ciutat` és el costat invers (`inverse="true"` / `mappedBy = "ciutat"`).
- **Relació lògica o física.** Amb SQLite, Hibernate crea la columna `ciutatId` però no
  la restricció de clau forana (la relació la gestiona Hibernate). Amb MySQL sí que es
  crea la `FOREIGN KEY`.
- **Mètodes d'ajuda** `addCiutada` / `removeCiutada` a `Ciutat`, que actualitzen els dos
  costats de la relació.
- **Sense cascade.** Esborrar una ciutat no esborra els seus ciutadans: es queden sense
  ciutat (`ciutatId` a `NULL`).
- **Manager amb el mateix estil que els repositoris d'exemple**: `executeInTransaction`
  i `executeInTransactionWithResult` (try-with-resources, commit i rollback).
- **Col·lecció LAZY**: `listCollection` la inicialitza abans de tancar la sessió i
  `getCiutatWithCiutadans` fa servir `JOIN FETCH`, per evitar `LazyInitializationException`.
- **`equals` / `hashCode`** basats en l'identificador, i `toString` de `Ciutada` sense
  la ciutat per evitar recursió infinita.
- A l'exercici 1, clau primària amb `GenerationType.IDENTITY` i `@ManyToOne` en `LAZY`.

## Hibernate 7.4 i SQLite: ordre de les columnes

`hibernate.properties` inclou `hibernate.column_ordering_strategy=legacy`. Sense aquesta
línia, Hibernate 7.4 crea la taula amb les columnes `integer` (`poblacio`, `edat`) abans
de la clau primària i, per un error en la generació del DDL per a SQLite, la clau es
crea sense tipus: deixa de ser autoincremental i es desa a `NULL`. El símptoma és que
`persist` retorna un identificador, però `find` i les consultes no troben res.

## Versions

Java 21, Hibernate ORM 7.4.16.Final, sqlite-jdbc 3.53.2.1, MySQL Connector/J 26.7.0,
JUnit Jupiter 6.1.3.

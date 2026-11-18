package com.project;

import java.util.Scanner;

/**
 * Exercici d'ampliació: menú per triar la base de dades abans d'executar
 * el programa principal.
 *
 *   ./run.sh com.project.MainMenu
 *
 * Per a MySQL cal tenir el contenidor engegat (carpeta docker/).
 */
public class MainMenu {
    public static void main(String[] args) {
        System.out.println("Tria la base de dades:");
        System.out.println("  1) SQLite (./data/database.db)");
        System.out.println("  2) MySQL  (docker, localhost:3008/test-mysql)");
        System.out.print("Opció [1]: ");

        Scanner scanner = new Scanner(System.in);
        String opcio = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
        System.out.println();

        if ("2".equals(opcio)) {
            Manager.setPropertiesFile("hibernate-mysql.properties");
        }
        Main.main(args);
    }
}

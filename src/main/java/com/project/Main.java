package com.project;

import java.util.concurrent.CompletableFuture;

public class Main {

    public static void main(String[] args) {

        System.out.println("Inici del programa");

        // Creem una cadena de tres tasques asíncrones amb CompletableFuture.
        // El resultat final és un CompletableFuture<Void> perquè l'última
        // tasca (thenAccept) només mostra dades i no retorna res.
        CompletableFuture<Void> cadena = CompletableFuture

            // TASCA 1: supplyAsync executa el codi en un altre fil
            // i retorna un valor. Simula la validació de la sol·licitud.
            .supplyAsync(() -> {
                System.out.println("Tasca 1: validant les dades de la sol·licitud...");
                // Aquest és el valor inicial que passarem a la tasca següent
                int valorInicial = 10;
                System.out.println("Tasca 1: dades validades, valor inicial = " + valorInicial);
                // El return envia el valor a la tasca 2
                return valorInicial;
            })

            // TASCA 2: thenApply s'executa quan la tasca 1 ha acabat.
            // Rep el valor de la tasca 1 (el paràmetre "valor"),
            // el modifica i retorna un nou valor.
            .thenApply(valor -> {
                System.out.println("Tasca 2: calculant el resultat amb el valor " + valor);
                // Fem un càlcul senzill amb el valor rebut
                int resultat = valor * 2 + 5;
                System.out.println("Tasca 2: resultat calculat = " + resultat);
                // Retornem el resultat per passar-lo a la tasca 3
                return resultat;
            })

            // TASCA 3: thenAccept s'executa quan la tasca 2 ha acabat.
            // Rep el resultat calculat però no retorna res,
            // només el mostra (simula la resposta enviada a l'usuari).
            .thenAccept(resultat -> {
                System.out.println("Tasca 3: enviant resposta a l'usuari...");
                System.out.println("Resposta final: el resultat és " + resultat);
            });

        // join() bloqueja el fil principal fins que tota la cadena s'ha completat.
        // Sense això, el programa podria acabar abans que les tasques
        // asíncrones hagin tingut temps d'executar-se.
        cadena.join();

        System.out.println("Fi del programa");
    }
}
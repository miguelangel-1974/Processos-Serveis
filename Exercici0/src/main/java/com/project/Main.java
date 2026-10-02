package com.project;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {

    public static void main(String[] args) {

        System.out.println("Inici del programa");

        // Estructura de dades concurrent que compartiran les tres tasques.
        // Guardarem el saldo del compte amb la clau "saldo".
        ConcurrentMap<String, Integer> dades = new ConcurrentHashMap<>();

        // Els CountDownLatch serveixen per fer que una tasca esperi a una altra.
        // Així ens assegurem que les tasques es fan en l'ordre correcte:
        // primer es rep l'operació, després es modifica i al final es llegeix.
        CountDownLatch operacioRebuda = new CountDownLatch(1);
        CountDownLatch saldoModificat = new CountDownLatch(1);

        // Executor amb un pool de 3 fils, un per cada tasca
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // TASCA 1 (Runnable): introdueix les dades inicials.
        // Simula la recepció d'una operació bancària (un ingrés de 1000).
        Runnable tasca1 = () -> {
            dades.put("saldo", 1000);
            System.out.println("Tasca 1: operacio rebuda, saldo inicial = 1000");
            // Avisem que ja hi ha dades perquè la tasca 2 pugui continuar
            operacioRebuda.countDown();
        };

        // TASCA 2 (Runnable): modifica les dades.
        // Simula el càlcul d'interessos i comissions.
        Runnable tasca2 = () -> {
            try {
                // Esperem que la tasca 1 hagi posat el saldo inicial
                operacioRebuda.await();

                int saldo = dades.get("saldo");

                // Sumem 50 d'interessos
                saldo = saldo + 50;
                System.out.println("Tasca 2: afegits 50 d'interessos");

                // Restem 10 de comissió
                saldo = saldo - 10;
                System.out.println("Tasca 2: restats 10 de comissio");

                // Guardem el nou saldo al mapa compartit
                dades.put("saldo", saldo);
                System.out.println("Tasca 2: saldo modificat = " + saldo);

                // Avisem que ja s'ha modificat perquè la tasca 3 pugui llegir
                saldoModificat.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        // TASCA 3 (Callable): llegeix les dades modificades
        // i retorna el saldo actualitzat.
        Callable<Integer> tasca3 = () -> {
            // Esperem que la tasca 2 hagi acabat de modificar el saldo
            saldoModificat.await();

            int saldoFinal = dades.get("saldo");
            System.out.println("Tasca 3: saldo llegit = " + saldoFinal);

            // Retornem el resultat final
            return saldoFinal;
        };

        // Enviem les tres tasques a l'executor.
        // Amb submit() del Callable obtenim un Future amb el resultat pendent.
        executor.execute(tasca1);
        executor.execute(tasca2);
        Future<Integer> resultat = executor.submit(tasca3);

        // Recollim el resultat del Callable. get() espera fins que estigui llest.
        try {
            System.out.println("Saldo actualitzat del client: " + resultat.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // Tanquem l'executor per alliberar els recursos
        executor.shutdown();

        System.out.println("Fi del programa");
    }
}
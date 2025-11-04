/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.insa.ynaho01.projetmessagerie.Utils;

import java.util.concurrent.locks.Condition;

/**
 *
 * @author marie
 */
public class utils {

    public static void sleepNoInterrupt(long dureeInMs) {
        try {
            Thread.sleep(dureeInMs);
        } catch (InterruptedException ex) {
            throw new Error("interruption non attendue");
        }
    }

    public static void waitNoInterrupt(Object verrou) {
        try {
            verrou.wait();
        } catch (InterruptedException ex) {
            throw new Error("interruption non attendue");
        }
    }

    public static void awaitNoInterrupt(Condition cond) {
        try {
            cond.await();
        } catch (InterruptedException ex) {
            throw new Error("interruption non attendue");
        }
    }

    public static void sleepAlea(long dureeBase) {
        try {
            double alea = Math.random() * 1.5 + 0.5;
            long duree = (long) alea * dureeBase;
            Thread.sleep(duree);
        } catch (InterruptedException ex) {
            throw new Error("interruption non attendue");
        }
    }

    public static void joinNoInterrupt(Thread aAttendre) {
        try {
            aAttendre.join();
        } catch (InterruptedException ex) {
            throw new Error("interruption non attendue");
        }
    }

}

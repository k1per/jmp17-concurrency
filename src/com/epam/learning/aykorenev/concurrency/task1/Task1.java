package com.epam.learning.aykorenev.concurrency.task1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Created by k1per on 15.06.17.
 */
public class Task1 {

    private static int counter = 0;
    private static void decrement() {
        counter--;
    }
    private static void increment() {
        counter++;
    }
    private static int getCounter() {
        return counter;
    }

    public static void main(String[] args) {

            ExecutorService service = Executors.newFixedThreadPool(1000);
            ReadWriteWLock readWriteWLock = new ReadWriteWLock();

            for (int i = 0; i < 300; i++) {
                service.submit(() -> {
                    try {
                        Thread.sleep(ThreadLocalRandom.current().nextInt(0,2000));
                        readWriteWLock.lockWrite();
                        increment();
                        readWriteWLock.unlockWrite();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                });
            }


            for (int i = 0; i < 3000; i++) {
                service.submit(() -> {
                    try {
                        Thread.sleep(ThreadLocalRandom.current().nextInt(0,3000));
                        readWriteWLock.lockRead();
                        int value = getCounter();
                        System.out.println(value);
                        readWriteWLock.unlockRead();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                });

            }

            for (int i = 0; i < 300; i++) {

                service.submit(() -> {
                    try {
                        Thread.sleep(ThreadLocalRandom.current().nextInt(0,2000));
                        readWriteWLock.lockWrite();
                        decrement();
                        readWriteWLock.unlockWrite();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                });

            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            service.shutdown();
            try {
                service.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Counter is " + getCounter());
        }

}

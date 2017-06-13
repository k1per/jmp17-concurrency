package com.epam.learning.aykorenev.concurrency;

import java.util.Random;

/**
 * We have two wrestlers in opposition. One increments counter and one decrements. Counter must not fall below zero.

 1. Run app. Observe how 'below zero' appears. Review the code and try to explain why it happens.

 2. Modify Counter class. Add coordination lock with wait/notify to avoid 'below zero' situation.
 */
public class Opposition {
    public class Counter {
        final Object lock = new Object();
        private int count = 10;

        public void increment() {
            synchronized (lock) {
                count++;
                lock.notify();
            }
        }
        public void decrement() {
            synchronized (lock) {
                int value = count - 1;
                if(value < 0){
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    count--;
                }
            }
        }
        public int get() {
            synchronized (lock) {
                return count;
            }
        }
    }

    public class Wrestler implements Runnable {
        private Counter counter;
        private boolean increment;
        private Random rand;

        public Wrestler(Counter counter, boolean increment) {
            this.counter = counter;
            this.increment = increment;
            rand = new Random();
        }
        @Override
        public void run() {
            while (true) {
                if (increment) {
                    counter.increment();
                } else {
                    counter.decrement();
                }

                int x = counter.get();
                if (x < 0) {
                    t1.interrupt();
                    t2.interrupt();
                    throw new IllegalStateException("We have below zero!");
                }

                System.out.println("Wrestler" + Thread.currentThread().getName() + " " + x);
                try {
                    Thread.sleep(rand.nextInt(100));
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

    }

    private Thread t1, t2;
    public static void main(String[] args) {
        new Opposition().start();
    }
    private void start() {
        Counter counter = new Counter();
        t1 = new Thread(new Wrestler(counter, true));
        t2 = new Thread(new Wrestler(counter, false));
        t1.start();
        t2.start();
        try {
            while (true) {
                Thread.sleep(100);
                if (!(t1.isAlive() && t2.isAlive())) {
                    break;
                }
            }
        } catch (InterruptedException e) {
        }
        System.out.println("Finished");
    }
}

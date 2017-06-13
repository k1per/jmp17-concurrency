package com.epam.learning.aykorenev.concurrency;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class ReadWriteWithoutLockApi {

    public static void main(String[] args) {

        ExecutorService service = Executors.newFixedThreadPool(1000);
        ReadWriteWithoutLockApi readWriteWithoutLockApi = new ReadWriteWithoutLockApi();
        for (int i = 0; i < 1000; i++) {
            service.submit(() -> {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(0,5000));
                    readWriteWithoutLockApi.lockWrite();
                    readWriteWithoutLockApi.increment();
                    readWriteWithoutLockApi.unlockWrite();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            });
        }


        for (int i = 0; i < 3000; i++) {
            service.submit(() -> {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(0,5000));
                    readWriteWithoutLockApi.lockRead();
                    int value = readWriteWithoutLockApi.getCounter();
                    System.out.println(value);
                    readWriteWithoutLockApi.unlockRead();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            });

        }

        for (int i = 0; i < 1000; i++) {

            service.submit(() -> {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(0,5000));
                    readWriteWithoutLockApi.lockWrite();
                    readWriteWithoutLockApi.decrement();
                    readWriteWithoutLockApi.unlockWrite();
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

        System.out.println("Counter is " + readWriteWithoutLockApi.getCounter());
    }

    private void decrement() {
        counter--;
    }

    private void increment() {
        counter++;
    }

    private int counter = 0;

    private Map<Thread, Integer> readingThreads = new HashMap<>();

    private int writeAccesses = 0;
    private int writeRequests = 0;
    private Thread writingThread = null;


    private synchronized void lockRead() throws InterruptedException {
        Thread callingThread = Thread.currentThread();
        while (!canGrantReadAccess(callingThread)) {
            wait();
        }

        readingThreads.put(callingThread,
                (getReadAccessCount(callingThread) + 1));
    }

    private boolean canGrantReadAccess(Thread callingThread) {
        return isWriter(callingThread) || !hasWriter() && (isReader(callingThread) || !hasWriteRequests());
    }


    private synchronized void unlockRead() {
        Thread callingThread = Thread.currentThread();
        if (!isReader(callingThread)) {
            throw new IllegalMonitorStateException("Calling Thread does not" +
                    " hold a read lock on this ReadWriteLock");
        }
        int accessCount = getReadAccessCount(callingThread);
        if (accessCount == 1) {
            readingThreads.remove(callingThread);
        } else {
            readingThreads.put(callingThread, (accessCount - 1));
        }
        notifyAll();
    }

    private synchronized void lockWrite() throws InterruptedException {
        writeRequests++;
        Thread callingThread = Thread.currentThread();
        while (!canGrantWriteAccess(callingThread)) {
            wait();
        }
        writeRequests--;
        writeAccesses++;
        writingThread = callingThread;
    }

    private synchronized void unlockWrite() throws InterruptedException {
        if (!isWriter(Thread.currentThread())) {
            throw new IllegalMonitorStateException("Calling Thread does not" +
                    " hold the write lock on this ReadWriteLock");
        }
        writeAccesses--;
        if (writeAccesses == 0) {
            writingThread = null;
        }
        notifyAll();
    }

    private boolean canGrantWriteAccess(Thread callingThread) {
        return isOnlyReader(callingThread) || !hasReaders() && (writingThread == null || isWriter(callingThread));
    }


    private int getReadAccessCount(Thread callingThread) {
        Integer accessCount = readingThreads.get(callingThread);
        if (accessCount == null) return 0;
        return accessCount;
    }


    private boolean hasReaders() {
        return readingThreads.size() > 0;
    }

    private boolean isReader(Thread callingThread) {
        return readingThreads.get(callingThread) != null;
    }

    private boolean isOnlyReader(Thread callingThread) {
        return readingThreads.size() == 1 &&
                readingThreads.get(callingThread) != null;
    }

    private boolean hasWriter() {
        return writingThread != null;
    }

    private boolean isWriter(Thread callingThread) {
        return writingThread == callingThread;
    }

    private boolean hasWriteRequests() {
        return this.writeRequests > 0;
    }

    private int getCounter() {
        return counter;
    }
}

package org.example.app;

import org.example.controller.MonitoringController;

import java.util.Scanner;

public class MonitoringLoop {
    static final int REFRESH_INTERVAL_SECONDS = 3;

    private final MonitoringController controller;
    private volatile boolean running;

    public MonitoringLoop(MonitoringController controller) {
        this.controller = controller;
    }

    public void start() {
        running = true;

        Thread inputThread = new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (running) {
                if (sc.hasNextLine() && sc.nextLine().trim().equals("q")) {
                    stop();
                }
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();

        while (running) {
            controller.refresh();
            try {
                Thread.sleep(REFRESH_INTERVAL_SECONDS * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        controller.shutdown();
    }

    public void stop() {
        running = false;
    }
}

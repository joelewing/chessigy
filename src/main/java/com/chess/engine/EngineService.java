package com.chess.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.function.Consumer;

import javafx.application.Platform;

public class EngineService {

    private Process process;
    private BufferedReader reader;
    private BufferedWriter writer;
    private Thread readerThread;
    private volatile boolean isRunning = false;

    private Consumer<String> onBestMove;
    private Consumer<String> onInfo;

    public void startEngine(String jarPath) throws IOException {
        if (isRunning) {
            return;
        }

        ProcessBuilder pb = new ProcessBuilder("java", "--add-modules", "jdk.incubator.vector", "-jar", jarPath);
        pb.redirectErrorStream(true);
        process = pb.start();

        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        isRunning = true;
        readerThread = new Thread(this::readLoop);
        readerThread.setDaemon(true);
        readerThread.start();

        sendCommand("uci");
        sendCommand("isready");
        sendCommand("ucinewgame");
    }

    public void stopEngine() {
        isRunning = false;
        if (process != null) {
            process.destroy();
        }
        try {
            if (reader != null)
                reader.close();
            if (writer != null)
                writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(String command) {
        if (!isRunning || writer == null)
            return;
        try {
            writer.write(command + "\n");
            writer.flush();
            System.out.println(">> " + command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setOnBestMove(Consumer<String> callback) {
        this.onBestMove = callback;
    }

    public void setOnInfo(Consumer<String> callback) {
        this.onInfo = callback;
    }

    private void readLoop() {
        try {
            String line;
            while (isRunning && (line = reader.readLine()) != null) {
                System.out.println("<< " + line);
                processLine(line);
            }
        } catch (IOException e) {
            if (isRunning) {
                e.printStackTrace();
            }
        }
    }

    private void processLine(String line) {
        if (line.startsWith("bestmove")) {
            String[] parts = line.split("\\s+");
            if (parts.length > 1) {
                String move = parts[1];
                if (onBestMove != null) {
                    Platform.runLater(() -> onBestMove.accept(move));
                }
            }
        } else if (line.startsWith("info")) {
            if (onInfo != null) {
                Platform.runLater(() -> onInfo.accept(line));
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}

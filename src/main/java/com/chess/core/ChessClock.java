package com.chess.core;

import javafx.animation.AnimationTimer;

/**
 * Manages chess clock timers for both White and Black players.
 * Uses JavaFX AnimationTimer for smooth countdown with nanosecond precision.
 */
public class ChessClock {
    private long whiteTimeNanos;
    private long blackTimeNanos;
    private PieceColor activeClock;
    private AnimationTimer timer;
    private long lastUpdateTime;

    private Runnable onTimeUpdate;
    private java.util.function.Consumer<PieceColor> onTimeExpired;

    /**
     * Creates a new chess clock with the specified time limit for each player.
     * 
     * @param minutesPerSide Time limit in minutes for each player
     */
    public ChessClock(int minutesPerSide) {
        this.whiteTimeNanos = minutesPerSide * 60L * 1_000_000_000L;
        this.blackTimeNanos = minutesPerSide * 60L * 1_000_000_000L;
        this.activeClock = null;

        this.timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (activeClock == null) {
                    return;
                }

                if (lastUpdateTime == 0) {
                    lastUpdateTime = now;
                    return;
                }

                long elapsed = now - lastUpdateTime;
                lastUpdateTime = now;

                if (activeClock == PieceColor.WHITE) {
                    whiteTimeNanos -= elapsed;
                    if (whiteTimeNanos <= 0) {
                        whiteTimeNanos = 0;
                        stop();
                        if (onTimeExpired != null) {
                            onTimeExpired.accept(PieceColor.WHITE);
                        }
                    }
                } else {
                    blackTimeNanos -= elapsed;
                    if (blackTimeNanos <= 0) {
                        blackTimeNanos = 0;
                        stop();
                        if (onTimeExpired != null) {
                            onTimeExpired.accept(PieceColor.BLACK);
                        }
                    }
                }

                if (onTimeUpdate != null) {
                    onTimeUpdate.run();
                }
            }
        };
    }

    /**
     * Starts the clock for the specified color.
     * 
     * @param color The color whose clock should run
     */
    public void start(PieceColor color) {
        this.activeClock = color;
        this.lastUpdateTime = 0;
        timer.start();
    }

    /**
     * Pauses the clock.
     */
    public void pause() {
        this.activeClock = null;
        this.lastUpdateTime = 0;
    }

    /**
     * Stops the clock completely.
     */
    public void stop() {
        timer.stop();
        this.activeClock = null;
        this.lastUpdateTime = 0;
    }

    /**
     * Switches the active clock to the opponent.
     */
    public void switchTurn() {
        if (activeClock == PieceColor.WHITE) {
            start(PieceColor.BLACK);
        } else if (activeClock == PieceColor.BLACK) {
            start(PieceColor.WHITE);
        }
    }

    /**
     * Gets the remaining time for the specified color in seconds.
     * 
     * @param color The color to get time for
     * @return Remaining time in seconds
     */
    public double getRemainingTimeSeconds(PieceColor color) {
        long nanos = color == PieceColor.WHITE ? whiteTimeNanos : blackTimeNanos;
        return nanos / 1_000_000_000.0;
    }

    /**
     * Checks if the specified color has run out of time.
     * 
     * @param color The color to check
     * @return true if time has expired
     */
    public boolean isExpired(PieceColor color) {
        return (color == PieceColor.WHITE ? whiteTimeNanos : blackTimeNanos) <= 0;
    }

    /**
     * Sets the callback to be invoked when time is updated.
     * 
     * @param callback The callback to invoke
     */
    public void setOnTimeUpdate(Runnable callback) {
        this.onTimeUpdate = callback;
    }

    /**
     * Sets the callback to be invoked when a player's time expires.
     * 
     * @param callback The callback to invoke with the color that expired
     */
    public void setOnTimeExpired(java.util.function.Consumer<PieceColor> callback) {
        this.onTimeExpired = callback;
    }

    /**
     * Formats the remaining time as MM:SS.
     * 
     * @param color The color to format time for
     * @return Formatted time string
     */
    public String formatTime(PieceColor color) {
        double seconds = getRemainingTimeSeconds(color);
        int minutes = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        return String.format("%d:%02d", minutes, secs);
    }
}

package com.chess.ui;

public class NewGameSettings {
    private final boolean vsComputer;
    private final boolean playAsWhite;
    private final String difficulty;
    private final boolean useTimeLimit;
    private final int timeMinutes;
    private final int incrementSeconds;
    private final String clockType;

    public NewGameSettings(boolean vsComputer, boolean playAsWhite, String difficulty,
            boolean useTimeLimit, int timeMinutes, int incrementSeconds, String clockType) {
        this.vsComputer = vsComputer;
        this.playAsWhite = playAsWhite;
        this.difficulty = difficulty;
        this.useTimeLimit = useTimeLimit;
        this.timeMinutes = timeMinutes;
        this.incrementSeconds = incrementSeconds;
        this.clockType = clockType;
    }

    public boolean isVsComputer() {
        return vsComputer;
    }

    public boolean isPlayAsWhite() {
        return playAsWhite;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public boolean isUseTimeLimit() {
        return useTimeLimit;
    }

    public int getTimeMinutes() {
        return timeMinutes;
    }

    public int getIncrementSeconds() {
        return incrementSeconds;
    }

    public String getClockType() {
        return clockType;
    }
}

package com.acconeer.bluetooth.presence.model;

public enum RadarCommand {
    RESET, SET;

    public static RadarCommand fromOrdinal(int ordinal) {
        switch (ordinal) {
            case 0:
                return RESET;
            case 1:
                return SET;
        }

        return null;
    }
}

package com.acconeer.bluetooth.distance.bluetooth.data;

public enum RadarCommand {
    RESET, SET, THRESHOLD_ESTIMATION;

    public static RadarCommand fromOrdinal(int ordinal) {
        switch (ordinal) {
            case 0:
                return RESET;
            case 1:
                return SET;
            case 2:
                return THRESHOLD_ESTIMATION;
        }

        return null;
    }
}

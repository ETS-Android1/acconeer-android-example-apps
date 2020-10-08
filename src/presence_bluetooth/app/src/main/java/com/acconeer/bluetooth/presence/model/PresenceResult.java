package com.acconeer.bluetooth.presence.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PresenceResult {
    public boolean presenceDetected;
    public float distance;
    public float presenceScore;

    public PresenceResult(boolean presenceDetected, float distance, float presenceScore) {
        this.presenceDetected = presenceDetected;
        this.distance = distance;
        this.presenceScore = presenceScore;
    }

    @Override
    public String toString() {
        return "PresenceResult{" +
                "presenceDetected=" + presenceDetected +
                ", distance=" + distance +
                ", presenceScore=" + presenceScore +
                '}';
    }

    public byte[] toByteArray() {
        return ByteBuffer.allocate(Float.BYTES * 3)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(presenceDetected ? 1 : 0)
                .putFloat(distance)
                .putFloat(presenceScore)
                .array();
    }
}

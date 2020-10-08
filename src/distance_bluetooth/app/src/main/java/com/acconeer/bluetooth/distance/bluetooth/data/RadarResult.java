package com.acconeer.bluetooth.distance.bluetooth.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class RadarResult {
    public float distance;
    public int signalStrength;
    public boolean isSaturated;

    public RadarResult(float distance, int signalStrength, boolean isSaturated) {
        this.distance = distance;
        this.signalStrength = signalStrength;
        this.isSaturated = isSaturated;
    }

    @Override
    public String toString() {
        return "RadarResult{" +
                "distance=" + distance +
                ", signalStrength=" + signalStrength +
                ", isSaturated=" + isSaturated +
                '}';
    }

    public byte[] toByteArray() {
        return ByteBuffer.allocate(Float.BYTES * 3)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(distance)
                .putInt(signalStrength)
                .putInt(isSaturated ? 1 : 0)
                .array();
    }
}

package com.acconeer.bluetooth.presence.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class RadarParameters {
    public float rangeStart;
    public float rangeLength;
    public float updateRate;
    public float fixedThreshold;

    public RadarParameters(float rangeStart, float rangeLength, float updateRate, float fixedThreshold) {
        this.rangeStart = rangeStart;
        this.rangeLength = rangeLength;
        this.updateRate = updateRate;
        this.fixedThreshold = fixedThreshold;
    }

    @Override
    public String toString() {
        return "RadarParameters{" +
                "rangeStart=" + rangeStart +
                ", rangeLength=" + rangeLength +
                ", updateRate=" + updateRate +
                ", fixedThreshold=" + fixedThreshold +
                '}';
    }

    public byte[] toByteArray() {
        return ByteBuffer.allocate(Float.BYTES * 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(rangeStart / 1000)
                .putFloat(rangeLength / 1000)
                .putFloat(updateRate)
                .putFloat(fixedThreshold)
                .array();
    }
}

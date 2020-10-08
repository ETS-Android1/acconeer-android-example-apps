package com.acconeer.bluetooth.distance.bluetooth.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class RadarParameters {
    public float rangeStart;
    public float rangeLength;
    public float gain;
    public float updateRate;
    public float fixedThreshold;

    public RadarParameters(float rangeStart, float rangeLength, float gain, float updateRate, float fixedThreshold) {
        this.rangeStart = rangeStart;
        this.rangeLength = rangeLength;
        this.gain = gain;
        this.updateRate = updateRate;
        this.fixedThreshold = fixedThreshold;
    }

    @Override
    public String toString() {
        return "RadarParameters{" +
                "rangeStart=" + rangeStart +
                ", rangeLength=" + rangeLength +
                ", gain=" + gain +
                ", updateRate=" + updateRate +
                ", fixedThreshold=" + fixedThreshold +
                '}';
    }

    public byte[] toByteArray() {
        return ByteBuffer.allocate(Float.BYTES * 5)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(rangeStart / 1000)
                .putFloat(rangeLength / 1000)
                .putFloat(gain)
                .putFloat(updateRate)
                .putInt((int) fixedThreshold)
                .array();
    }
}

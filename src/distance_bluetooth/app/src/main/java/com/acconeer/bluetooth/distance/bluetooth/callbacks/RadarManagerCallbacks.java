package com.acconeer.bluetooth.distance.bluetooth.callbacks;

import no.nordicsemi.android.ble.BleManagerCallbacks;

public interface RadarManagerCallbacks extends BleManagerCallbacks, RssServiceProfile,
    ParametersProfile, ResultsProfile, CommandProfile {

}

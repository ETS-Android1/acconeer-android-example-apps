package com.acconeer.bluetooth.presence.bluetooth.callbacks;

import no.nordicsemi.android.ble.BleManagerCallbacks;

public interface RadarManagerCallbacks extends BleManagerCallbacks, RssServiceProfile,
    ParametersProfile, ResultsProfile, CommandProfile {

}

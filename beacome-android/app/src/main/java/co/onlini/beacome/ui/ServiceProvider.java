package co.onlini.beacome.ui;

import android.app.Service;

import co.onlini.beacome.bluetooth.ScannerService;

public interface ServiceProvider {
    void getScanningService(ServiceDst<ScannerService> dst);


    interface ServiceDst<T extends Service> {
        void onServiceReady(T service);
    }
}

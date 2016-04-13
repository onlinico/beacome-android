package co.onlini.beacome.util;

import android.util.Log;

import java.util.Arrays;
import java.util.UUID;

import co.onlini.beacome.model.BeaconInfo;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class BeaconDataParser {
    private static final int DEVICE_TYPE_IBEACON = 0x0215;
    private static final int DEVICE_TYPE_ALT_BEACON = 0xBEAC;
    private static final int DEVICE_TYPE_EDDYSTIONE_UID = 0x00;
    private static final int DEVICE_TYPE_EDDYSTIONE_URL = 0x01;
    private static final int DEVICE_TYPE_EDDYSTIONE_TLM = 0x02;
    private static final String TAG = BeaconDataParser.class.getSimpleName();

    public static BeaconInfo parseEddystoneBeaconData(byte[] data, ScanResult scanResult) {
        BeaconInfo beaconInfo = null;
        if (data != null) {
            int flag = data[0] & 0xff;
            switch (flag) {
                case DEVICE_TYPE_EDDYSTIONE_UID:
                    // Bytes 0-17 contain data, 18-19 reserved.
                    if (data.length > 17 && data.length < 21) {
                        int txPower = data[1];
                        byte[] namespace = Arrays.copyOfRange(data, 2, 12);
                        byte[] instanceId = Arrays.copyOfRange(data, 12, 18);
                        double distance = DataUtil.calculateAccuracy(txPower, scanResult.getRssi());
                        byte[] uuidBytes = new byte[namespace.length + instanceId.length];
                        System.arraycopy(namespace, 0, uuidBytes, 0, namespace.length);
                        System.arraycopy(instanceId, 0, uuidBytes, namespace.length, instanceId.length);
                        UUID uuid = DataUtil.getGuidFromByteArray(uuidBytes);
                        beaconInfo = new BeaconInfo(scanResult.getDevice().getAddress(), txPower, scanResult.getRssi(), uuid.toString().toLowerCase(), 0, 0, System.currentTimeMillis());
                    } else {
                        Log.e(TAG, "Unknown data format");
                    }
                    break;
                case DEVICE_TYPE_EDDYSTIONE_URL:
                case DEVICE_TYPE_EDDYSTIONE_TLM:
                    break;
                default:
                    Log.e(TAG, "Unknown data structure");
                    break;
            }
        }
        return beaconInfo;
    }

    public static BeaconInfo parseAppleBeaconData(byte[] data, ScanResult scanResult) {
        BeaconInfo beaconInfo = null;
        if (data != null) {
            byte[] deviceType = Arrays.copyOfRange(data, 0, 2);
            int typeInt = DataUtil.byteArrayToInt(deviceType);
            switch (typeInt) {
                case DEVICE_TYPE_IBEACON:
                case DEVICE_TYPE_ALT_BEACON:
                    if (data.length == 23 || data.length == 24) {
                        byte[] uuidBytes = Arrays.copyOfRange(data, 2, 18);
                        byte[] major = Arrays.copyOfRange(data, 18, 20);
                        byte[] minor = Arrays.copyOfRange(data, 20, 22);
                        int power = data[22];
                        UUID uuid = DataUtil.getGuidFromByteArray(uuidBytes);
                        double distance = DataUtil.getDistance(scanResult.getRssi(), power);
                        beaconInfo = new BeaconInfo(scanResult.getDevice().getAddress(), power, scanResult.getRssi(), uuid.toString().toLowerCase(), DataUtil.byteArrayToInt(major), DataUtil.byteArrayToInt(minor), System.currentTimeMillis());
                    }
                    break;
            }
        }
        return beaconInfo;
    }

}

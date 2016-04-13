package co.onlini.beacome.util;

import java.nio.ByteBuffer;
import java.util.UUID;

public class DataUtil {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHexStr(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static int byteArrayToInt(byte[] b) {
        int value = 0;
        for (byte aB : b) {
            value = (value << 8) + (aB & 0xFF);
        }
        return value;
    }

    public static byte[] shortToByteArray(short value) {
        return new byte[]{
                (byte) (value >>> 8),
                (byte) value};
    }

    public static byte[] uuidToByteArray(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    public static UUID getGuidFromByteArray(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long high = bb.getLong();
        long low = bb.getLong();
        return new UUID(high, low);
    }

    public static double getDistance(int txPower, int rssi) {
        return Math.pow(10d, ((double) txPower - rssi) / (10 * 2));
    }

    public static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0 || txPower == Integer.MIN_VALUE) {
            return -1.0;
        }

        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            return (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
        }
    }
}

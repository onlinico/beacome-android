package co.onlini.beacome.util;

import android.text.InputFilter;
import android.text.Spanned;

public class HexCharsInputFilter implements InputFilter {
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        for (int i = start; i < end; i++) {
            char c = source.charAt(i);
            if (!(c >= 'a' && c <= 'f') && !(c >= 'A' && c <= 'F') && !(c >= '0' && c <= '9') && !(c == ' ') && !(c == '-')) {
                return "";
            }
        }
        return null;
    }
}

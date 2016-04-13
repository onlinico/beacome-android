package co.onlini.beacome.util;

import android.util.Patterns;

import java.util.regex.Pattern;


public class InputDataValidation {

    private static final Pattern mPhonePattern = Pattern.compile("^[+]?[0-9]{10,13}$");

    public static boolean isEmailValid(String email) {
        if (email != null) {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
        return false;
    }

    public static boolean isPhoneValid(String phoneNumber) {
        if (phoneNumber != null && phoneNumber.length() > 9 && phoneNumber.length() < 14) {
            return mPhonePattern.matcher(phoneNumber).matches()
                    && Patterns.PHONE.matcher(phoneNumber).matches();
        }
        return false;
    }

    public static boolean isUrlValid(String url) {
        if (url != null) {
            return Patterns.WEB_URL.matcher(url).matches();
        }
        return false;
    }
}

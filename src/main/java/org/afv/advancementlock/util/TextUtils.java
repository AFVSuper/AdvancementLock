package org.afv.advancementlock.util;

public class TextUtils {
    public static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input; // return as is if null or empty
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
}

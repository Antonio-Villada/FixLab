package com.software.fixlab.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EmailMaskUtil {

    public static String enmascarar(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return "***";
        }
        int at = email.indexOf('@');
        String local = email.substring(0, at);
        String domain = email.substring(at + 1);
        if (local.length() <= 1) {
            return "****@" + domain;
        }
        return local.charAt(0) + "***@" + domain;
    }
}

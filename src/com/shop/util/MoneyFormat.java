package com.shop.util;

import java.util.Locale;

public class MoneyFormat {

    private MoneyFormat() {
    }

    public static String format(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
}

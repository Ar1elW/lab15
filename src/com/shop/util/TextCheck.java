package com.shop.util;

public class TextCheck {

    private TextCheck() {
    }

    public static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
        if (value.contains(";") || value.contains(":") || value.contains(",")) {
            throw new IllegalArgumentException(fieldName + " не должно содержать символы ; : ,");
        }
        return value.trim();
    }
}

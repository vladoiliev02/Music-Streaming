package com.vlado.spotify.validations;

public class ParameterValidator {
    private static final String NULL_PARAM_ERROR = "Parameter %s cannot be null.";
    private static final String EMPTY_PARAM_ERROR = "Parameter %s cannot be empty.";
    private static final String BLANK_PARAM_ERROR = "Parameter %s cannot be blank.";
    private static final String POSITIVE_PARAM_ERROR = "Parameter %s must be positive.";
    private static final String NON_NEGATIVE_PARAM = "Parameter %s must be non negative.";

    public static <T> T checkNull(T obj, String paramName) {
        if (obj == null) {
            throw new IllegalArgumentException(String.format(NULL_PARAM_ERROR, paramName));
        }

        return obj;
    }

    public static String checkEmpty(String string, String paramName) {
        if (string.isEmpty()) {
            throw new IllegalArgumentException(String.format(EMPTY_PARAM_ERROR, paramName));
        }

        return string;
    }

    public static String checkBlank(String string, String paramName) {
        if (string.isBlank()) {
            throw new IllegalArgumentException(String.format(BLANK_PARAM_ERROR, paramName));
        }

        return string;
    }

    public static int checkPositive(int number, String paramName) {
        if (number <= 0) {
            throw new IllegalArgumentException(String.format(POSITIVE_PARAM_ERROR, paramName));
        }

        return number;
    }

    public static int checkNonNegative(int number, String paramName) {
        if (number < 0) {
            throw new IllegalArgumentException(String.format(NON_NEGATIVE_PARAM, paramName));
        }

        return number;
    }


}

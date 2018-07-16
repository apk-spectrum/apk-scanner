package com.apkscanner.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.apkscanner.annotations.NonNull;

public class ApkScannerVersion {
    public static final ApkScannerVersion UNKNOWN = new ApkScannerVersion(-1, -1, -1);

    /** Matches e.g. ".... 1.0.32" */
    private static final Pattern APKSCANNER_VERSION_PATTERN = Pattern.compile(
            "^.*(\\d+)\\.(\\d+)\\.(\\d+).*");

    public final int major;
    public final int minor;
    public final int micro;

    private ApkScannerVersion(int major, int minor, int micro) {
        this.major = major;
        this.minor = minor;
        this.micro = micro;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%1$d.%2$d.%3$d", major, minor, micro);
    }

    public int compareTo(ApkScannerVersion o) {
        if (major != o.major) {
            return major - o.major;
        }

        if (minor != o.minor) {
            return minor - o.minor;
        }

        return micro - o.micro;
    }

    @NonNull
    public static ApkScannerVersion parseFrom(@NonNull String input) {
        Matcher matcher = APKSCANNER_VERSION_PATTERN.matcher(input);
        if (matcher.matches()) {
            int major = Integer.parseInt(matcher.group(1));
            int minor = Integer.parseInt(matcher.group(2));
            int micro = Integer.parseInt(matcher.group(3));
            return new ApkScannerVersion(major, minor, micro);
        } else {
            return UNKNOWN;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ApkScannerVersion version = (ApkScannerVersion) o;

        if (major != version.major) {
            return false;
        }
        if (minor != version.minor) {
            return false;
        }
        return micro == version.micro;

    }

    @Override
    public int hashCode() {
        int result = major;
        result = 31 * result + minor;
        result = 31 * result + micro;
        return result;
    }

}

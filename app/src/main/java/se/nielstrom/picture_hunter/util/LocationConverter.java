package se.nielstrom.picture_hunter.util;

import android.location.Location;

public class LocationConverter {

    private static final int denomDeg = 1;
    private static final int denomMin = 1;
    private static final int denomSec = 1000;

    public static String getLatitudeRef(double latitude) {
        return latitude > 0 ? "N" : "S";
    }

    public static String getLongitudeRef(double longitude) {
        return longitude > 0 ? "E" : "W";
    }

    public static int getSign(String ref) {
        if (ref.equals("N") || ref.equals("E")) {
            return 1;
        } else {
            return -1;
        }
    }

    public static String toDMS(double decimal) {
        double degrees = decimal;
        double minutes = (60 * (decimal - Math.floor(degrees)));
        double seconds = (3600 * (decimal - Math.floor(degrees) - (Math.floor(minutes)/60)));


        // Format as "num1/denom1,num2/denom2,num3/denom3"
        StringBuilder builder = new StringBuilder();
        builder.append((int) (degrees*denomDeg));
        builder.append("/" + denomDeg + ",");
        builder.append((int) (minutes*denomMin));
        builder.append("/" + denomMin + ",");
        builder.append((int) Math.round(seconds*denomSec));
        builder.append("/" + denomSec);

        return builder.toString();
    }


    public static double fromDMS(String dms) {
        String[] parts = dms.split(",");

        double degrees = convertDMSPart(parts[0]);
        double minutes = convertDMSPart(parts[1]) / 60;
        double seconds = convertDMSPart(parts[2]) / 3600;

        return degrees + minutes + seconds;
    }

    private static double convertDMSPart(String part) {
        String[] operands = part.split("/");
        return Double.valueOf(operands[0]) / Double.valueOf(operands[1]);
    }
}

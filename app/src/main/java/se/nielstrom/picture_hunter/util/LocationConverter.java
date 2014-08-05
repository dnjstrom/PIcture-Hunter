package se.nielstrom.picture_hunter.util;

import android.location.Location;

/**
 * Utility class which knows how to convert between the DMS coordinate format used by the EXIF
 * metadata and the decimal coordinate format used by Android.
 */
public class LocationConverter {

    //The number of extra decimal places stored. (100=2, 1000=3 extra decimal places)
    private static final int denomDeg = 1;
    private static final int denomMin = 1;
    private static final int denomSec = 1000;

    /**
     * Returns whether the latitude is on the north or south hemisphere.
     *
     * @param latitude The latitude in decimal format.
     * @return "N" for north, "S" for south
     */
    public static String getLatitudeRef(double latitude) {
        return latitude > 0 ? "N" : "S";
    }

    /**
     * Returns whether the longitude is on the East or West hemisphere.
     * @param longitude The longitude in decimal format
     * @return "E" for east, "W" for west.
     */
    public static String getLongitudeRef(double longitude) {
        return longitude > 0 ? "E" : "W";
    }

    /**
     * Returns the sign for a decimal format based on the ref.
     *
     * @param ref "N", "S", "E" or "W"
     * @return 1 if "N" or "E", -1 otherwise
     */
    public static int getSign(String ref) {
        if (ref.equals("N") || ref.equals("E")) {
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * Converts a coordinate in the decimal format to a coordinate in the Degree-Minute-Second
     * format.
     *
     * @param decimal a decimal coordinate, either latitude or longitude.
     * @return A string in the DMS format
     */
    public static String toDMS(double decimal) {
        double degrees = decimal;
        // 60 minutes in one degree
        double minutes = (60 * (decimal - Math.floor(degrees)));
        // 60 seconds in one minute
        double seconds = (3600 * (decimal - Math.floor(degrees) - (Math.floor(minutes)/60)));


        // Format as "num1/denom1,num2/denom2,num3/denom3"
        // where "denomX" denotes by what value to divide the "numX" by
        // when converting back.
        StringBuilder builder = new StringBuilder();
        builder.append((int) (degrees*denomDeg));
        builder.append("/" + denomDeg + ",");

        builder.append((int) (minutes*denomMin));
        builder.append("/" + denomMin + ",");

        builder.append((int) Math.round(seconds*denomSec));
        builder.append("/" + denomSec);

        return builder.toString();
    }


    /**
     * Converts a coordinate in the Degree-Minute-Second format to a coordinate in the decimal format.
     * @param dms A String in the DMS-format
     * @return An unsigned coordinate in decimal format. See: {@link #getSign(String)}
     */
    public static double fromDMS(String dms) {
        String[] parts = dms.split(",");

        double degrees = convertDMSPart(parts[0]);
        double minutes = convertDMSPart(parts[1]) / 60;
        double seconds = convertDMSPart(parts[2]) / 3600;

        return degrees + minutes + seconds;
    }

    /**
     * Extracts a numX/denomX pair and executes the division.
     *
     * @param part
     * @return The result of dividing numX by denomX
     */
    private static double convertDMSPart(String part) {
        String[] operands = part.split("/");
        return Double.valueOf(operands[0]) / Double.valueOf(operands[1]);
    }
}

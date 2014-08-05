package se.nielstrom.picture_hunter.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;

import java.io.File;

/**
 * Compares two bitmaps by sampling 25 color averages.
 */
public class ImageComparator {

    private ResultCallback callback;

    /**
     * The only public facing action.
     * @param a Reference image file
     * @param b Other image file
     * @param callback Class to call with the result
     */
    public void compare(File a, File b, ResultCallback callback) {
        new ImageComparatorWorker().execute(a, b);
        this.callback = callback;
    }

    /**
     * Does the actual comparison asynchronously.
     */
    private class ImageComparatorWorker extends AsyncTask<File, Void, Double> {

        // A sampling will contain PIXEL_SAMPLE_SIZE^2 values
        public static final int PIXEL_SAMPLE_SIZE = 5;

        File fileA;
        File fileB;

        @Override
        protected Double doInBackground(File... files) {
            // Extract the two files to be compared
            if (files == null || files.length < 2) {
                cancel(true);
                return null;
            } else {
                fileA = files[0];
                fileB = files[1];
            }

            // Decode files into bitmaps
            Bitmap a = BitmapFactory.decodeFile(fileA.getAbsolutePath());
            Bitmap b = BitmapFactory.decodeFile(fileB.getAbsolutePath());

            // Recover averaged color samples
            int[][] sampleA = sampleImage(a);
            int[][] sampleB = sampleImage(b);

            // Calculate the distance between sample sets
            return calculateDistance(sampleA, sampleB);
        }

        @Override
        protected void onPostExecute(Double distance) {
            callback.onComparisonResult(fileA, fileB, distance);
        }

        /**
         * Iterates over the bitmap pixels and collects an averaged color value from each
         * point.
         *
         * @param img the bitmap to sample
         * @return a 2d array of color-integers.
         */
        private int[][] sampleImage(Bitmap img) {
            int[][] pixels = new int[PIXEL_SAMPLE_SIZE][PIXEL_SAMPLE_SIZE];

            // calculate the number of pixels to travers on the x-axis between samplings
            int stepX = img.getWidth()/PIXEL_SAMPLE_SIZE;
            for (int x = 1; x<img.getWidth()/stepX; x++) {

                // calculate the number of pixels to travers on the y-axis between samplings
                int stepY = img.getHeight()/PIXEL_SAMPLE_SIZE;
                for (int y = 1; y<img.getHeight()/stepY; y++) {
                    // Calculate an average color value based on surrounding pixels.
                    pixels[x][y] = averageAround(img, x*stepX, y*stepY);
                }
            }

            return pixels;
        }

        /**
         * Given a bitmap and a pixel position, calculate the average color in an area around the
         * pixel.
         *
         * @param img The bitmap
         * @param px x-position of the pixel
         * @param py y-position of the pixel
         * @return an averaged color-integer
         */
        private int averageAround(Bitmap img, int px, int py) {
            // The ranged sampled in each direction around the pixel
            final int sampleRange = 15;

            float sampledPixels = 0;
            int red = 0;
            int green = 0;
            int blue = 0;

            // traverse each pixel in the area around the posiiton.
            for (int x=px-sampleRange; x<px+sampleRange; x++) {
                for (int y=py-sampleRange; y<py+sampleRange; y++) {
                    try {
                        // Add color values to a total
                        int color = img.getPixel(x, y);
                        red += Color.red(color);
                        green += Color.green(color);
                        blue += Color.blue(color);
                        sampledPixels++;
                    } catch (IllegalArgumentException e) {
                        // pixel out of bounds, ignore.
                    }
                }
            }

            // Average the rgb channels and create a proper color int.
            return Color.rgb(
                Math.round(red/sampledPixels),
                Math.round(green/sampledPixels),
                Math.round(blue/sampledPixels)
            );
        }


        /**
         * Calculates the total distance between two color samples by measuring the distance
         * between each positional pair of colors in the set.
         * @param a
         * @param b
         * @return the total distance between the samples, often around 1-3k.
         */
        private double calculateDistance(int[][] a, int[][] b) {
            double distance = 0;

            // Traverse the two sets simultaneously
            for (int x = 0; x < a.length && x < b.length; x++) {
                for (int y = 0; y < a[x].length && y < b[x].length; y++) {
                    //Calculate the distance between the points and add to a total.
                    distance += calculateColorDistance(a[x][y], b[x][y]);
                }
            }

            return distance;
        }

        /**
         * Calcualtes the distance between two colors by calculating the length of a straight
         * line from one to the other in a 3d space with red, green and blue being the three axis
         * and black the origin point.
         *
         * @param a
         * @param b
         * @return the distance in 3d space
         */
        private double calculateColorDistance(int a, int b) {
            // basically, this is the pythagorean theorem
            return Math.sqrt(Math.pow(Color.red(a) - Color.red(b), 2)
                           + Math.pow(Color.green(a) - Color.green(b), 2)
                           + Math.pow(Color.blue(a) - Color.blue(b), 2));
        }
    }

    /**
     * Specifies a class which should be notified with the comparison result.
     */
    public interface ResultCallback {
        public void onComparisonResult(File a, File b, double distance);
    }
}

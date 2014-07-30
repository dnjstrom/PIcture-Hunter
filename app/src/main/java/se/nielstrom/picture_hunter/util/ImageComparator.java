package se.nielstrom.picture_hunter.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;

import java.io.File;

import se.nielstrom.picture_hunter.fragments.CameraFragment;


public class ImageComparator {

    private ResultCallback callback;

    public void compare(File a, File b, ResultCallback callback) {
        new ImageComparatorWorker().execute(a, b);
        this.callback = callback;
    }

    private class ImageComparatorWorker extends AsyncTask<File, Void, Double> {

        public static final int PIXEL_SAMPLE_SIZE = 5;

        File fileA;
        File fileB;

        @Override
        protected Double doInBackground(File... files) {
            if (files == null || files.length < 2) {
                cancel(true);
                return null;
            } else {
                fileA = files[0];
                fileB = files[1];
            }

            Bitmap a = BitmapFactory.decodeFile(fileA.getAbsolutePath());
            Bitmap b = BitmapFactory.decodeFile(fileB.getAbsolutePath());

            int[][] sampleA = sampleImage(a);
            int[][] sampleB = sampleImage(b);

            return calculateDistance(sampleA, sampleB);
        }

        @Override
        protected void onPostExecute(Double distance) {
            callback.onComparisonResult(fileA, fileB, distance);
        }

        private int[][] sampleImage(Bitmap img) {
            int[][] pixels = new int[PIXEL_SAMPLE_SIZE][PIXEL_SAMPLE_SIZE];

            int stepX = img.getWidth()/PIXEL_SAMPLE_SIZE;
            for (int x = 1; x<img.getWidth()/stepX; x++) {

                int stepY = img.getHeight()/PIXEL_SAMPLE_SIZE;
                for (int y = 1; y<img.getHeight()/stepY; y++) {
                    pixels[x][y] = averageAround(img, x*stepX, y*stepY);
                }
            }

            return pixels;
        }

        private int averageAround(Bitmap img, int px, int py) {
            final int sampleRange = 15;
            float sampledPixels = 0;
            int red = 0;
            int green = 0;
            int blue = 0;

            for (int x=px-sampleRange; x<px+sampleRange; x++) {
                for (int y=py-sampleRange; y<py+sampleRange; y++) {
                    try {
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

            return Color.rgb(
                Math.round(red/sampledPixels),
                Math.round(green/sampledPixels),
                Math.round(blue/sampledPixels)
            );
        }


        private double calculateDistance(int[][] a, int[][] b) {
            double distance = 0;

            for (int x = 0; x < a.length && x < b.length; x++) {
                for (int y = 0; y < a[x].length && y < b[x].length; y++) {
                    distance += calculateColorDistance(a[x][y], b[x][y]);
                }
            }

            return distance;
        }

        private double calculateColorDistance(int a, int b) {
            return Math.sqrt(Math.pow(Color.red(a) - Color.red(b), 2)
                           + Math.pow(Color.green(a) - Color.green(b), 2)
                           + Math.pow(Color.blue(a) - Color.blue(b), 2));
        }
    }

    public interface ResultCallback {
        public void onComparisonResult(File a, File b, double distance);
    }
}

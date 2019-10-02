package com.weis.darklaf.ui.colorchooser;

import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;

public class ColorWheelImageProducer extends MemoryImageSource {

    private final int[] myPixels;
    private final int myWidth;
    private final int myHeight;
    private float myBrightness;

    private float[] myHues;
    private float[] mySat;
    private int[] myAlphas;

    public ColorWheelImageProducer(final int w, final int h, final float brightness) {
        super(w, h, null, 0, w);
        myPixels = new int[w * h];
        myWidth = w;
        myHeight = h;
        myBrightness = brightness;
        generateLookupTables();
        newPixels(myPixels, ColorModel.getRGBdefault(), 0, w);
        setAnimated(true);
        generateColorWheel();
    }

    public int getRadius() {
        return Math.min(myWidth, myHeight) / 2 - 2;
    }

    private void generateLookupTables() {
        mySat = new float[myWidth * myHeight];
        myHues = new float[myWidth * myHeight];
        myAlphas = new int[myWidth * myHeight];
        float radius = getRadius();

        // blend is used to create a linear alpha gradient of two extra pixels
        float blend = (radius + 2f) / radius - 1f;

        // Center of the color wheel circle
        int cx = myWidth / 2;
        int cy = myHeight / 2;

        for (int x = 0; x < myWidth; x++) {
            int kx = x - cx; // cartesian coordinates of x
            int squarekx = kx * kx; // Square of cartesian x

            for (int y = 0; y < myHeight; y++) {
                int ky = cy - y; // cartesian coordinates of y

                int index = x + y * myWidth;
                mySat[index] = (float) Math.sqrt(squarekx + ky
                                                            * ky)
                               / radius;
                if (mySat[index] <= 1f) {
                    myAlphas[index] = 0xff000000;
                } else {
                    myAlphas[index] = (int) ((blend - Math.min(blend,
                                                               mySat[index] - 1f)) * 255 / blend) << 24;
                    mySat[index] = 1f;
                }
                if (myAlphas[index] != 0) {
                    myHues[index] = (float) (Math.atan2(ky, kx) / Math.PI / 2d);
                }
            }
        }
    }

    public void generateColorWheel() {
        for (int index = 0; index < myPixels.length; index++) {
            if (myAlphas[index] != 0) {
                myPixels[index] = myAlphas[index] | 0xffffff & Color.HSBtoRGB(myHues[index],
                                                                              mySat[index],
                                                                              myBrightness);
            }
        }
        newPixels();
    }
}

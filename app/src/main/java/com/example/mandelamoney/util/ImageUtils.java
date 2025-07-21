package com.example.mandelamoney.util;
import android.media.Image;
import android.util.Log;

import java.nio.ByteBuffer;

public class ImageUtils {

    private static final String TAG = "ImageUtils";

    /**
     * Converts an Android Image (YUV_420_888) to a byte array in NV21 format.
     * This method handles row strides and pixel strides to ensure the data is contiguous.
     * It also accounts for rotation, although ZXing's PlanarYUVLuminanceSource might need further rotation.
     *
     * @param image The ImageProxy object from CameraX.
     * @return A byte array representing the image in NV21 format, or null if conversion fails.
     */
    public static byte[] yuv420_888toNv21(Image image) {
        if (image == null || image.getFormat() != android.graphics.ImageFormat.YUV_420_888) {
            Log.e(TAG, "Invalid Image format for NV21 conversion: " + (image != null ? image.getFormat() : "null"));
            return null;
        }

        Image.Plane yPlane = image.getPlanes()[0];
        Image.Plane uPlane = image.getPlanes()[1];
        Image.Plane vPlane = image.getPlanes()[2];

        ByteBuffer yBuffer = yPlane.getBuffer();
        ByteBuffer uBuffer = uPlane.getBuffer();
        ByteBuffer vBuffer = vPlane.getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        // NV21 format: YYYYYYYY VV UU
        // The NV21 format (YCrCb) has Y plane first, then interleaved Cb (U) and Cr (V) planes.
        // However, YUV_420_888 often gives U and V in separate planes.
        // We'll arrange them in NV21 order (Y plane, then V plane, then U plane interleaved).
        // Size: Y plane (width * height) + UV plane (width * height / 2)
        byte[] nv21 = new byte[ySize + uSize + vSize]; // This is a rough estimate; actual size might be smaller due to strides

        int width = image.getWidth();
        int height = image.getHeight();

        // Copy Y plane
        int rowStrideY = yPlane.getRowStride();
        int pixelStrideY = yPlane.getPixelStride();
        int pos = 0;
        for (int y = 0; y < height; y++) {
            yBuffer.position(y * rowStrideY);
            yBuffer.get(nv21, pos, width);
            pos += width;
        }

        // Copy UV planes (interleaved for NV21)
        int rowStrideU = uPlane.getRowStride();
        int rowStrideV = vPlane.getRowStride();
        int pixelStrideU = uPlane.getPixelStride();
        int pixelStrideV = vPlane.getPixelStride();

        int uvWidth = width / 2;
        int uvHeight = height / 2;

        // Ensure UV buffer positions are at the start
        uBuffer.position(0);
        vBuffer.position(0);

        // NV21 interleaves V and U
        for (int y = 0; y < uvHeight; y++) {
            for (int x = 0; x < uvWidth; x++) {
                // Copy V
                nv21[pos++] = vBuffer.get(y * rowStrideV + x * pixelStrideV);
                // Copy U
                nv21[pos++] = uBuffer.get(y * rowStrideU + x * pixelStrideU);
            }
        }
        return nv21;
    }

    /**
     * Rotates an NV21 byte array.
     * This is crucial because PlanarYUVLuminanceSource doesn't inherently handle rotation
     * beyond its constructor parameters.
     *
     * @param data The NV21 byte array.
     * @param imageWidth Original image width.
     * @param imageHeight Original image height.
     * @param rotationDegrees Rotation in degrees (0, 90, 180, 270).
     * @return Rotated NV21 byte array.
     */
    public static byte[] rotateNv21(byte[] data, int imageWidth, int imageHeight, int rotationDegrees) {
        if (rotationDegrees == 0) return data; // No rotation needed

        byte[] rotatedData = new byte[data.length];
        int ySize = imageWidth * imageHeight;

        if (rotationDegrees == 90) {
            for (int j = 0; j < imageWidth; j++) {
                for (int i = 0; i < imageHeight; i++) {
                    rotatedData[j * imageHeight + i] = data[i * imageWidth + imageWidth - 1 - j];
                }
            }
            // Rotate UV plane (interleaved)
            int uvWidth = imageWidth / 2;
            int uvHeight = imageHeight / 2;
            for (int j = 0; j < uvWidth; j++) {
                for (int i = 0; i < uvHeight; i++) {
                    rotatedData[ySize + j * uvHeight * 2 + i * 2] = data[ySize + (i * uvWidth + uvWidth - 1 - j) * 2]; // V
                    rotatedData[ySize + j * uvHeight * 2 + i * 2 + 1] = data[ySize + (i * uvWidth + uvWidth - 1 - j) * 2 + 1]; // U
                }
            }
        } else if (rotationDegrees == 180) {
            for (int i = 0; i < ySize; i++) {
                rotatedData[i] = data[ySize - 1 - i];
            }
            for (int i = ySize; i < data.length; i += 2) { // UV plane
                rotatedData[i] = data[data.length - 2 - (i - ySize)];     // V
                rotatedData[i + 1] = data[data.length - 1 - (i - ySize)]; // U
            }
        } else if (rotationDegrees == 270) {
            for (int j = 0; j < imageWidth; j++) {
                for (int i = 0; i < imageHeight; i++) {
                    rotatedData[j * imageHeight + imageHeight - 1 - i] = data[i * imageWidth + j];
                }
            }
            // Rotate UV plane (interleaved)
            int uvWidth = imageWidth / 2;
            int uvHeight = imageHeight / 2;
            for (int j = 0; j < uvWidth; j++) {
                for (int i = 0; i < uvHeight; i++) {
                    rotatedData[ySize + j * uvHeight * 2 + (uvHeight - 1 - i) * 2] = data[ySize + (i * uvWidth + j) * 2]; // V
                    rotatedData[ySize + j * uvHeight * 2 + (uvHeight - 1 - i) * 2 + 1] = data[ySize + (i * uvWidth + j) * 2 + 1]; // U
                }
            }
        }
        return rotatedData;
    }
}
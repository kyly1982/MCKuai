package com.mckuai.imc.Utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by kyly on 2016/1/27.
 */
public class BitmapUtil {
    public static Bitmap decodeFile(Context context, Uri uri, int width, int height) {
        String path = getFilePath(context, uri);
        Bitmap bitmap = getPreviewBitmap(path, width, height);
        if (bitmap.getHeight() != height || bitmap.getWidth() != width) {
            return cutBitmap(bitmap, width, height);
        }
        return bitmap;
    }

    public static Bitmap decodeFile(String fileName, int width, int height) {
        Bitmap bitmap = getPreviewBitmap(fileName, width, height);
        if (bitmap.getHeight() != height || bitmap.getWidth() != width) {
            return cutBitmap(bitmap, width, height);
        }
        return bitmap;
    }

    private static String getFilePath(Context context, Uri uri) {
        String path = null;
        String[] files = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, files, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            path = cursor.getString(column_index);
        }
        cursor.close();
        return path;
    }

    private static Bitmap cutBitmap(Bitmap bitmap, int width, int height) {
        int left = 0, top = 0;
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        int minSideLength = bitmapHeight > bitmapWidth ? bitmapWidth : bitmapHeight;
        if (bitmap.getWidth() != bitmap.getHeight()) {
            bitmapWidth = bitmap.getWidth();
            bitmapHeight = bitmap.getHeight();
            if (bitmapWidth > bitmapHeight) {
                left = (bitmapWidth - bitmapHeight) / 2;
            } else {
                top = (bitmapHeight - bitmapWidth) / 2;
            }
        }
        Matrix matrix = new Matrix();
        float scale = ((float) width) / minSideLength;
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, left, top, minSideLength, minSideLength, matrix, true);
    }

    private static Bitmap getPreviewBitmap(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        final int minSideLength = width >= height ? height : width;
        options.inSampleSize = computeSampleSize(options, minSideLength, width * height);
        options.inJustDecodeBounds = false;
        options.inInputShareable = true;
        options.inPurgeable = true;
        try {
            return BitmapFactory.decodeFile(path, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int computeSampleSize(BitmapFactory.Options options,
                                         int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math
                .floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }
}

package com.toolslab.quickcode.util.bitmap;


import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.toolslab.quickcode.util.UriUtils;
import com.toolslab.quickcode.util.log.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PdfToBitmapConverter {

    private PdfToBitmapConverter() {
        // Hide utility class constructor
    }

    @NonNull
    @CheckResult
    public static List<Bitmap> pdfUriToBitmaps(ContentResolver contentResolver, Uri uri) {
        if (!deviceSupportsPdfToBitmap()) {
            Logger.logError("Build version " + Build.VERSION.SDK_INT +
                    " does not support android.graphics.pdf.PdfRenderer: " + uri);
            return new ArrayList<>();
        } else {
            return convertPdfUriToBitmaps(contentResolver, uri);
        }
    }

    public static boolean deviceSupportsPdfToBitmap() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    @NonNull
    private static List<Bitmap> convertPdfUriToBitmaps(ContentResolver contentResolver, Uri uri) {
        List<Bitmap> bitmaps = new ArrayList<>();
        ParcelFileDescriptor fileDescriptor = null;
        PdfRenderer renderer = null;
        try {
            fileDescriptor = contentResolver.openFileDescriptor(uri, UriUtils.MODE_READ);
            if (fileDescriptor != null) {
                renderer = new PdfRenderer(fileDescriptor);
                int pageCount = renderer.getPageCount();
                for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                    Bitmap bitmap = getBitmapFromPage(renderer, pageIndex);
                    if (bitmap != null) {
                        bitmaps.add(bitmap);
                    }
                }
            }
        } catch (IOException e) {
            Logger.logException(e);
        } finally {
            if (renderer != null) {
                renderer.close();
            }
            if (fileDescriptor != null) {
                try {
                    fileDescriptor.close();
                } catch (IOException e) {
                    Logger.logException(e);
                }
            }
        }
        return bitmaps;
    }

    @Nullable
    @CheckResult
    private static Bitmap getBitmapFromPage(PdfRenderer renderer, int pageIndex) {
        PdfRenderer.Page page = null;
        try {
            page = renderer.openPage(pageIndex);
            Bitmap bitmap = BitmapUtils.createWhiteBitmap(page.getWidth(), page.getHeight());
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            return bitmap;
        } catch (Exception e) {
            Logger.logException(e);
        } finally {
            if (page != null) {
                page.close();
            }
        }
        return null;
    }

}

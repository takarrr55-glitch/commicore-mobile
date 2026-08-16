package com.commicore.mobile;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public final class MediaHelper {
    private MediaHelper(){}

    public static Uri latestVideo(Context c) {
        Uri base = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATE_ADDED
        };
        Cursor cur = null;
        try {
            cur = c.getContentResolver().query(base, projection, null, null,
                MediaStore.Video.Media.DATE_ADDED + " DESC");
            if(cur != null && cur.moveToFirst()) {
                long id = cur.getLong(0);
                return Uri.withAppendedPath(base, String.valueOf(id));
            }
        } catch(Exception ignored) {
        } finally {
            if(cur != null) cur.close();
        }
        return null;
    }
}

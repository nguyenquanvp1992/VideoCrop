package com.easy.editvideo.playback;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.OptIn;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class CropRepository {

    private BehaviorSubject<VideoSize> videoSizeSubject = BehaviorSubject.create();

    private final Context context;

    public CropRepository(Context context) {
        this.context = context;
    }

    @OptIn(markerClass = UnstableApi.class)
    public void getVideoSize(String uri) {
        String[] projection = new String[]{MediaStore.Video.Media.WIDTH, MediaStore.Video.Media.HEIGHT};
        Cursor cursor = context.getContentResolver().query(Uri.parse(uri), projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH));
                int height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT));
                videoSizeSubject.onNext(new VideoSize(width, height));
            }
            cursor.close();
        }
    }

    public Observable<VideoSize> videoSize() {
        return videoSizeSubject.hide();
    }
}
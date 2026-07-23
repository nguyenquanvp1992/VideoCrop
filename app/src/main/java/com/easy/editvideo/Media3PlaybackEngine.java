package com.easy.editvideo;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.Consumer;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

@UnstableApi
public final class Media3PlaybackEngine {

    private static final long TICK_INTERVAL_MS = 100L;

    private final ExoPlayer player;
    private final PublishSubject<PlaybackEvent> eventSubject = PublishSubject.create();
    private final PublishSubject<VideoSize> videoSizeSubject = PublishSubject.create();
    private boolean released = false;

    public Consumer<VideoSize> videoSizeListener;

    @MainThread
    public Media3PlaybackEngine(Context context, Consumer<VideoSize> videoSizeListener) {
        player = new ExoPlayer.Builder(context).build();
        player.setRepeatMode(Player.REPEAT_MODE_OFF);
        player.addListener(buildPlayerListener());
        player.setVideoEffects(new ArrayList<>());
        this.videoSizeListener = videoSizeListener;
    }

    @MainThread
    public void loadSegments(@NonNull String videoUri) {
        if (released) return;
        List<MediaItem> items = buildPlaylist(videoUri);
        player.setMediaItems(items);
        player.prepare();
    }

    @MainThread
    public void play() {
        if (released) return;
        player.play();
    }

    @MainThread
    public void pause() {
        if (released) return;
        player.pause();
    }

    @MainThread
    public void seekTo(long positionMs) {
        if (released) return;
        player.seekTo(positionMs);
    }

    public long getCurrentPositionMs() {
        if (released) return 0L;
        return player.getCurrentPosition();
    }

    public long getDurationMs() {
        if (released) return 0L;
        return player.getDuration();
    }

    public boolean isPlaying() {
        return !released && player.isPlaying();
    }

    public void attachSurface(@NonNull Object surfaceHolder) {
        if (released) return;
        if (surfaceHolder instanceof android.view.SurfaceView) {
            player.setVideoSurfaceView((android.view.SurfaceView) surfaceHolder);
        } else if (surfaceHolder instanceof android.view.TextureView) {
            player.setVideoTextureView((android.view.TextureView) surfaceHolder);
        }
    }

    public void detachSurface() {
        if (released) return;
        player.clearVideoSurface();
    }

    @NonNull
    public Observable<Long> positionTicker() {
        return Observable
                .interval(TICK_INTERVAL_MS, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(ignored -> getCurrentPositionMs())
                .takeUntil(eventSubject.filter(e -> e == PlaybackEvent.ERROR));
    }

    @NonNull
    public Observable<PlaybackEvent> events() {
        return eventSubject.hide();
    }

    @NonNull
    public Observable<VideoSize> videoSizeEvents() {
        return videoSizeSubject.hide();
    }

    @MainThread
    public void release() {
        if (released) return;
        released = true;
        player.release();
        eventSubject.onComplete();
        videoSizeSubject.onComplete();
    }

    public ExoPlayer getExoPlayer() {
        return player;
    }

    @NonNull
    private List<MediaItem> buildPlaylist(@NonNull String videoUri) {
        Uri uri = Uri.parse(videoUri);
        return List.of(new MediaItem.Builder()
                .setUri(uri)
                .setClippingConfiguration(new MediaItem.ClippingConfiguration.Builder().build()).build());
    }

    @NonNull
    private Player.Listener buildPlayerListener() {
        return new Player.Listener() {

            @Override
            public void onVideoSizeChanged(@NonNull androidx.media3.common.VideoSize videoSize) {
                if (released) return;
                if (videoSize.width > 0 && videoSize.height > 0) {
                    videoSizeSubject.onNext(new VideoSize(videoSize.width, videoSize.height));
                    videoSizeListener.accept(videoSize);
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (released) return;
                eventSubject.onNext(isPlaying ? PlaybackEvent.PLAYING : PlaybackEvent.PAUSED);
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (released) return;
                switch (playbackState) {
                    case Player.STATE_BUFFERING:
                        eventSubject.onNext(PlaybackEvent.BUFFERING_START);
                        break;
                    case Player.STATE_READY:
                        eventSubject.onNext(PlaybackEvent.BUFFERING_END);
                        break;
                    case Player.STATE_ENDED:
                        eventSubject.onNext(PlaybackEvent.ENDED);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                if (released) return;
                eventSubject.onNext(PlaybackEvent.ERROR);
            }

            @Override
            public void onSurfaceSizeChanged(int width, int height) {
                Player.Listener.super.onSurfaceSizeChanged(width, height);
                Log.d("NguyenQuan", "onSurfaceSizeChanged: " + width + " x " + height);
            }
        };
    }
}
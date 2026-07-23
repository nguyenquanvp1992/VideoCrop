package com.easy.editvideo.playback;

import android.app.Application;

import androidx.annotation.OptIn;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;

import com.easy.editvideo.CropAspectRatio;
import com.easy.editvideo.CropInfo;
import com.easy.editvideo.CropState;
import com.easy.editvideo.VideoScale;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class CropViewModel extends AndroidViewModel {

    private final MutableLiveData<CropAspectRatio> _cropState = new MutableLiveData<>(CropAspectRatio.ORIGINAL);
    public final LiveData<CropAspectRatio> cropState = _cropState;

    private final MutableLiveData<CropInfo> _cropInfoEditing = new MutableLiveData<>(CropInfo.NONE);
    public final LiveData<CropInfo> cropInfoEditing = _cropInfoEditing;
    private final MutableLiveData<CropInfo> _cropInfoCommit = new MutableLiveData<>(CropInfo.NONE);
    public final LiveData<CropInfo> cropInfoCommit = _cropInfoCommit;
    private final MutableLiveData<CropState> _processState = new MutableLiveData<>(CropState.NONE);
    public final LiveData<CropState> processState = _processState;
    private final MutableLiveData<String> _uriVideo = new MutableLiveData<>();
    public final LiveData<String> uriVideo = _uriVideo;
    private final MutableLiveData<VideoSize> _cropSize = new MutableLiveData<>();
    public final LiveData<VideoSize> cropSize = _cropSize;
    private final MutableLiveData<VideoScale> _videoScale = new MutableLiveData<>();
    public final LiveData<VideoScale> videoScale = _videoScale;

    private final CropRepository repository;
    private CompositeDisposable disposable = new CompositeDisposable();

    private VideoSize videoSize;
    private VideoSize parentVideoSize;

    private final BehaviorSubject<VideoSize> videoSizeSubject = BehaviorSubject.create();
    private final BehaviorSubject<VideoSize> parentVideoSizeSubject = BehaviorSubject.create();

    public CropViewModel(Application application) {
        super(application);
        repository = new CropRepository(application.getApplicationContext());
        disposable.add(repository.videoSize().subscribe(videoSize -> {
            this.videoSize = videoSize;
            calculatorCropVideoPort();
        }));
//        videoSizeSubject.combineLatest(parentVideoSizeSubject.hide()).hide();
    }

    @OptIn(markerClass = UnstableApi.class)
    public void setParentSize(int width, int height) {
        parentVideoSize = new VideoSize(width, height);
        calculatorCropVideoPort();
    }

    public void setUriVideo(String uriVideo) {
        _uriVideo.postValue(uriVideo);
        repository.getVideoSize(uriVideo);
    }

    public void selectCrop(CropAspectRatio cropAspectRatio) {
        _cropState.postValue(cropAspectRatio);
    }

    public void resetViewport() {
        calculatorCropVideoPort();
    }

    public void commit(CropInfo cropInfo) {
        _cropInfoCommit.postValue(cropInfo);
        _processState.postValue(CropState.NONE);
        calculatorVideoFrame(cropInfo);
    }

    public void startEditing() {
        _processState.postValue(CropState.EDITING);
    }

    public void cancelEditing() {
        _processState.postValue(CropState.NONE);
    }

    public void applyCrop() {
        _processState.postValue(CropState.APPLY);
    }

    @OptIn(markerClass = UnstableApi.class)
    private void calculatorVideoFrame(CropInfo cropInfo) {
        float widthVideoCrop = (videoSize.width * (cropInfo.getRight() - cropInfo.getLeft()));
        float heightVideoCrop = (videoSize.height * (cropInfo.getBottom() - cropInfo.getTop()));
        float scaleX = widthVideoCrop / parentVideoSize.width;
        float scaleY = heightVideoCrop / parentVideoSize.height;
        float scale = Math.max(scaleX, scaleY);
        int width = (int) (widthVideoCrop / scale);
        int height = (int) (heightVideoCrop / scale);
        VideoSize newSize = new VideoSize(width, height);
        _cropSize.postValue(newSize);
        calculatorCropScale(cropInfo, newSize);
    }

    @OptIn(markerClass = UnstableApi.class)
    private void calculatorCropVideoPort() {
        if (videoSize == null || parentVideoSize == null) {
            return;
        }
        float scaleX = videoSize.width * 1f / parentVideoSize.width;
        float scaleY = videoSize.height * 1f / parentVideoSize.height;
        float originalScale = Math.max(scaleX, scaleY);
        _cropSize.postValue(new VideoSize((int) (videoSize.width / originalScale), (int) (videoSize.height / originalScale)));
        _videoScale.postValue(new VideoScale(0, 0, 1f));
    }

    private void calculatorCropScale(CropInfo cropInfo, VideoSize newSize) {
        float videoScale = Math.max(videoSize.width * 1f / newSize.width, videoSize.height * 1f / newSize.height);
        float widthVideo = videoSize.width / videoScale;
        float heighVideo = videoSize.height / videoScale;
        float scale = Math.max(newSize.width / widthVideo, newSize.height / heighVideo);
        float widthVideoAfterScale = widthVideo * scale;
        float heightVideoAfterScale = heighVideo * scale;

        float centerXCropVideo = widthVideoAfterScale * (cropInfo.getLeft() + cropInfo.getRight()) / 2;
        float centerYCropVideo = heightVideoAfterScale * (cropInfo.getTop() + cropInfo.getBottom()) / 2;

        int translationX = (int) (widthVideoAfterScale / 2 - centerXCropVideo);
        int translationY = (int) (heightVideoAfterScale / 2 - centerYCropVideo);
        _videoScale.postValue(new VideoScale(translationX, translationY, scale));
    }
}
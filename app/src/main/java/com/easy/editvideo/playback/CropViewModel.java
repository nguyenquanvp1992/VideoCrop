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
import com.easy.editvideo.ViewSize;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class CropViewModel extends AndroidViewModel {

    private final MutableLiveData<CropAspectRatio> _cropState = new MutableLiveData<>(CropAspectRatio.ORIGINAL);
    public final LiveData<CropAspectRatio> cropState = _cropState;

    private final MutableLiveData<CropInfo> _cropInfoEditing = new MutableLiveData<>(CropInfo.NONE);
    public final LiveData<CropInfo> cropInfoEditing = _cropInfoEditing;
    private final MutableLiveData<CropState> _processState = new MutableLiveData<>(CropState.NONE);
    public final LiveData<CropState> processState = _processState;
    private final MutableLiveData<String> _uriVideo = new MutableLiveData<>();
    public final LiveData<String> uriVideo = _uriVideo;
    private final MutableLiveData<ViewSize> _cropSize = new MutableLiveData<>();
    public final LiveData<ViewSize> cropSize = _cropSize;

    private final CropRepository repository;
    private CompositeDisposable disposable = new CompositeDisposable();

    private final BehaviorSubject<ViewSize> parentVideoSizeSubject = BehaviorSubject.create();
    private final BehaviorSubject<CropInfo> cropInfoSubject = BehaviorSubject.create();

    public CropViewModel(Application application) {
        super(application);
        repository = new CropRepository(application.getApplicationContext());
        BehaviorSubject<VideoSize> videoSizeSubject = BehaviorSubject.create();
        disposable.add(repository.videoSize().subscribe(videoSizeSubject::onNext));
        disposable.add(Observable.combineLatest(
                        videoSizeSubject.distinctUntilChanged(),
                        parentVideoSizeSubject.distinctUntilChanged(),
                        cropInfoSubject.distinctUntilChanged(),
                        this::calculatorVideoSize)
                .distinctUntilChanged()
                .subscribe(_cropSize::postValue));
    }

    @OptIn(markerClass = UnstableApi.class)
    public void setParentSize(int width, int height) {
        ViewSize old = parentVideoSizeSubject.getValue();
        if (old == null) {
            old = new ViewSize();
        }
        parentVideoSizeSubject.onNext(old.withSize(width, height));
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
        _processState.postValue(CropState.NONE);
        cropInfoSubject.onNext(cropInfo);
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

    /*@OptIn(markerClass = UnstableApi.class)
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
    }*/

    @OptIn(markerClass = UnstableApi.class)
    private void calculatorCropVideoPort() {
        /*if (videoSize == null || parentVideoSize == null) {
            return;
        }
        float scaleX = videoSize.width * 1f / parentVideoSize.width;
        float scaleY = videoSize.height * 1f / parentVideoSize.height;
        float originalScale = Math.max(scaleX, scaleY);
        _cropSize.postValue(new VideoSize((int) (videoSize.width / originalScale), (int) (videoSize.height / originalScale)));
        _videoScale.postValue(new VideoScale(0, 0, 1f));*/
        cropInfoSubject.onNext(CropInfo.NONE);
    }

    /*private void calculatorCropScale(CropInfo cropInfo, VideoSize newSize) {
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
    }*/

    private ViewSize calculatorVideoSize(VideoSize videoSize, ViewSize parentSize, CropInfo cropInfo) {
        // Calculator crop frame
        float widthVideoCrop = (videoSize.width * (cropInfo.getRight() - cropInfo.getLeft()));
        float heightVideoCrop = (videoSize.height * (cropInfo.getBottom() - cropInfo.getTop()));
        float scaleCrop = Math.max(widthVideoCrop / parentSize.getWidth(), heightVideoCrop / parentSize.getHeight());
        int widthCrop = (int) (widthVideoCrop / scaleCrop);
        int heightCrop = (int) (heightVideoCrop / scaleCrop);

        // Calculator Preview Scale
        float scaleVideo = Math.max(videoSize.width * 1f / widthCrop, videoSize.height * 1f / heightCrop);
        float widthVideo = videoSize.width / scaleVideo;
        float heightVideo = videoSize.height / scaleVideo;
        float previewScale = Math.max(widthCrop / widthVideo , heightCrop / heightVideo);
        float widthVideoAfterScale = videoSize.width / previewScale;
        float heightVideoAfterScale = videoSize.height / previewScale;

        float centerXCropVideo = widthVideoAfterScale * (cropInfo.getLeft() + cropInfo.getRight()) / 2;
        float centerYCropVideo = heightVideoAfterScale * (cropInfo.getTop() + cropInfo.getBottom()) / 2;

        int translationX = (int) (widthVideoAfterScale / 2 - centerXCropVideo);
        int translationY = (int) (heightVideoAfterScale / 2 - centerYCropVideo);
        return new ViewSize().withScale(previewScale).withTranslation(translationX, translationY).withSize(widthCrop, heightCrop);
    }
}
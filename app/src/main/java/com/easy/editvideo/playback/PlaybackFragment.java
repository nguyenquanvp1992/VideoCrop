package com.easy.editvideo.playback;

import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;

import com.easy.editvideo.CropState;
import com.easy.editvideo.Media3PlaybackEngine;
import com.easy.editvideo.databinding.FragmentPlaybackBinding;

@OptIn(markerClass = UnstableApi.class)
public class PlaybackFragment extends Fragment {

    private FragmentPlaybackBinding binding;
    private Media3PlaybackEngine media3Playback;
    private CropViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(CropViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPlaybackBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        media3Playback = new Media3PlaybackEngine(requireContext(), this::updateVideoViewPort);
        handleResponse();
        handleLayoutChange();
    }

    private void initMedia3(String uri) {
        media3Playback.loadSegments(uri);
        ExoPlayer player = media3Playback.getExoPlayer();
        binding.player.setPlayer(player);
        player.setPlayWhenReady(true);
    }

    private void handleResponse() {
        viewModel.cropState.observe(getViewLifecycleOwner(), cropAspectRatio -> binding.cropOverlay.setAspectRatio(cropAspectRatio));
        viewModel.cropInfoEditing.observe(getViewLifecycleOwner(), cropInfo -> binding.cropOverlay.setCropInfo(cropInfo));
        viewModel.cropInfoCommit.observe(getViewLifecycleOwner(), cropInfo -> {
            // TODO
        });
        viewModel.processState.observe(getViewLifecycleOwner(), processState -> {
            if (processState == CropState.EDITING) {
                binding.cropOverlay.setVisibility(View.VISIBLE);
                viewModel.resetViewport();
            } else if (processState == CropState.APPLY) {
                binding.cropOverlay.setVisibility(View.GONE);
                viewModel.commit(binding.cropOverlay.getCurrentCropInfo());
            } else if (processState == CropState.NONE) {
                binding.cropOverlay.setVisibility(View.GONE);
            }
        });
        viewModel.uriVideo.observe(getViewLifecycleOwner(), this::initMedia3);
        viewModel.cropSize.observe(getViewLifecycleOwner(), this::appyCropRatio);
        viewModel.videoScale.observe(getViewLifecycleOwner(), videoScale -> {
            binding.player.setScaleX(videoScale.getScale());
            binding.player.setScaleY(videoScale.getScale());
            binding.player.setTranslationX(videoScale.getTranslationX());
            binding.player.setTranslationY(videoScale.getTranslationY());

            binding.player.getPlayer().play();
            new Handler(Looper.getMainLooper()).postDelayed(() -> binding.player.getPlayer().pause(), 100);
        });
    }

    private void handleLayoutChange() {
        binding.layoutPlayer.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> binding.cropOverlay.setVideoViewport(new RectF(0, 0, right - left, bottom - top)));
        binding.layoutRoot.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.layoutRoot.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = binding.layoutRoot.getWidth();
                int height = binding.layoutRoot.getHeight();
                viewModel.setParentSize(width, height);
            }
        });
    }

    private void updateVideoViewPort(VideoSize videoSize) {

    }

    private void appyCropRatio(VideoSize size) {
        ConstraintLayout.LayoutParams containerParams = (ConstraintLayout.LayoutParams) binding.layoutPlayer.getLayoutParams();
        containerParams.dimensionRatio = size.width + ":" + size.height;
        binding.layoutPlayer.setLayoutParams(containerParams);
//        binding.player.getPlayer().play();
//        new Handler(Looper.getMainLooper()).postDelayed(() -> binding.player.getPlayer().pause(), 100);
    }
}
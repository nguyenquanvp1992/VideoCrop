package com.easy.editvideo.playback;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.easy.editvideo.CropAspectRatio;
import com.easy.editvideo.CropState;
import com.easy.editvideo.databinding.FragmentCropBinding;

public class CropToolsFragment extends Fragment {

    private FragmentCropBinding binding;
    private CropViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(CropViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCropBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        handleResponse();
        binding.original.setOnClickListener(v -> viewModel.selectCrop(CropAspectRatio.ORIGINAL));
        binding.manual.setOnClickListener(v -> viewModel.selectCrop(CropAspectRatio.FREE));
        binding.crop11.setOnClickListener(v -> viewModel.selectCrop(CropAspectRatio.RATIO_1_1));
        binding.crop916.setOnClickListener(v -> viewModel.selectCrop(CropAspectRatio.RATIO_9_16));
        binding.crop169.setOnClickListener(v -> viewModel.selectCrop(CropAspectRatio.RATIO_16_9));
        binding.crop45.setOnClickListener(v -> viewModel.selectCrop(CropAspectRatio.RATIO_4_5));
        binding.crop54.setOnClickListener(v -> viewModel.selectCrop(CropAspectRatio.RATIO_5_4));
        binding.crop34.setOnClickListener(v -> viewModel.selectCrop(CropAspectRatio.RATIO_3_4));
        binding.crop43.setOnClickListener(v -> viewModel.selectCrop(CropAspectRatio.RATIO_4_3));
        handleClick();
    }

    private void updateSelect(CropAspectRatio aspectRatio) {
        binding.original.setSelected(aspectRatio == CropAspectRatio.ORIGINAL);
        binding.manual.setSelected(aspectRatio == CropAspectRatio.FREE);
        binding.crop11.setSelected(aspectRatio == CropAspectRatio.RATIO_1_1);
        binding.crop916.setSelected(aspectRatio == CropAspectRatio.RATIO_9_16);
        binding.crop169.setSelected(aspectRatio == CropAspectRatio.RATIO_16_9);
        binding.crop45.setSelected(aspectRatio == CropAspectRatio.RATIO_4_5);
        binding.crop54.setSelected(aspectRatio == CropAspectRatio.RATIO_5_4);
        binding.crop34.setSelected(aspectRatio == CropAspectRatio.RATIO_3_4);
        binding.crop43.setSelected(aspectRatio == CropAspectRatio.RATIO_4_3);
    }

    private void handleClick() {
        binding.actionDone.setOnClickListener(v -> viewModel.applyCrop());
        binding.actionBack.setOnClickListener(v -> viewModel.cancelEditing());
        binding.start.setOnClickListener(v -> viewModel.startEditing());
    }

    private void handleResponse() {
        viewModel.cropState.observe(getViewLifecycleOwner(), this::updateSelect);
        viewModel.processState.observe(getViewLifecycleOwner(), state -> {
            binding.start.setVisibility(state == CropState.EDITING ? View.GONE : View.VISIBLE);
            binding.layoutTool.setVisibility(state == CropState.EDITING ? View.VISIBLE : View.GONE);
        });
    }
}
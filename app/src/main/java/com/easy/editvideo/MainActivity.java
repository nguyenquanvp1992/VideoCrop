package com.easy.editvideo;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.util.UnstableApi;

import com.easy.editvideo.databinding.ActivityMainBinding;
import com.easy.editvideo.playback.CropViewModel;

@UnstableApi
public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String[]> readVideoPermission = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
        if (isGranted.containsValue(true)) {
            selectVideo();
        }
    });
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    setUriVideo(uri.toString());
                }
            });
    private CropViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewModel = new ViewModelProvider(this).get(CropViewModel.class);
        checkPermission();
    }

    private void checkPermission() {
        String[] permissionRequire = MediaPermissionHelper.getRequiredPermissions();
        if (!MediaPermissionHelper.hasVideoReadPermission(this)) {
            readVideoPermission.launch(permissionRequire);
        } else {
            selectVideo();
        }
    }

    private void selectVideo() {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE)
                .build());
//        setUriVideo("content://media/picker/0/com.android.providers.media.photopicker/media/216");
    }

    private void setUriVideo(String uri) {
        viewModel.setUriVideo(uri);
    }
}
package com.easy.editvideo;

public enum CropAspectRatio {
    FREE(0f, 0f),
    ORIGINAL(0f, 0f),
    RATIO_1_1(1f, 1f),
    RATIO_4_5(4f, 5f),
    RATIO_5_4(5f, 4f),
    RATIO_9_16(9f, 16f),
    RATIO_16_9(16f, 9f),
    RATIO_3_4(3f, 4f),
    RATIO_4_3(4f, 3f);

    public final float widthRatio;
    public final float heightRatio;

    CropAspectRatio(float widthRatio, float heightRatio) {
        this.widthRatio = widthRatio;
        this.heightRatio = heightRatio;
    }

    public boolean isFixed() {
        return widthRatio > 0f && heightRatio > 0f;
    }

    public String getRatio() {
        return (int) widthRatio + ":" + (int) heightRatio;
    }
}

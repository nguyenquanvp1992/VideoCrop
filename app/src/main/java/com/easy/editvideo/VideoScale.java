package com.easy.editvideo;

public class VideoScale {

    private final float scale;
    private final int translationX;
    private final int translationY;

    public VideoScale(int translationX, int translationY, float scale) {
        this.scale = scale;
        this.translationX = translationX;
        this.translationY = translationY;
    }

    public float getScale() {
        return scale;
    }

    public int getTranslationX() {
        return translationX;
    }

    public int getTranslationY() {
        return translationY;
    }
}
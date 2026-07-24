package com.easy.editvideo;

import androidx.annotation.Nullable;

import java.util.Objects;

public class ViewSize {

    private final int height;
    private final int width;
    private final float scaleX;
    private final float scaleY;
    private final float translationX;
    private final float translationY;

    public ViewSize() {
        this(0, 0, 1f, 1f, 0f, 0f);
    }

    private ViewSize(int width, int height, float scaleX, float scaleY, float translationX, float translationY) {
        this.height = height;
        this.width = width;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.translationX = translationX;
        this.translationY = translationY;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof ViewSize) {
            return width == ((ViewSize) obj).width &&
                    height == ((ViewSize) obj).height &&
                    scaleX == ((ViewSize) obj).scaleX &&
                    scaleY == ((ViewSize) obj).scaleY &&
                    translationX == ((ViewSize) obj).translationX &&
                    translationY == ((ViewSize) obj).translationY;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, scaleX, scaleY, translationX, translationY);
    }

    public ViewSize withScale(float scale) {
        return new ViewSize(width, height, scale, scale, translationX, translationY);
    }

    public ViewSize withTranslation(float translationX, float translationY) {
        return new ViewSize(width, height, scaleX, scaleY, translationX, translationY);
    }

    public ViewSize withSize(int width, int height) {
        return new ViewSize(width, height, scaleX, scaleY, translationX, translationY);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public float getTranslationX() {
        return translationX;
    }

    public float getTranslationY() {
        return translationY;
    }
}
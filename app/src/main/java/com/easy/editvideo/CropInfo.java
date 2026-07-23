package com.easy.editvideo;

public final class CropInfo {

    public static final CropInfo NONE = new CropInfo(0f, 0f, 1f, 1f);
    private final float left;
    private final float top;
    private final float right;
    private final float bottom;

    public CropInfo(float left, float top, float right, float bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public float getLeft() {
        return left;
    }

    public float getTop() {
        return top;
    }

    public float getRight() {
        return right;
    }

    public float getBottom() {
        return bottom;
    }

    public boolean isNone() {
        return left == 0f && top == 0f && right == 1f && bottom == 1f;
    }
}
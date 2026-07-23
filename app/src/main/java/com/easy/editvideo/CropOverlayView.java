package com.easy.editvideo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class CropOverlayView extends View {

    private static final float HANDLE_TOUCH_RADIUS_DP = 28f;
    private static final float HANDLE_DRAW_RADIUS_DP = 10f;
    private static final float BORDER_STROKE_DP = 2f;
    private static final float GRID_STROKE_DP = 1f;
    private static final int GRID_COLUMNS = 3;
    private static final int GRID_ROWS = 3;
    private static final float MIN_CROP_FRACTION = 0.1f;

    private final Paint dimPaint = new Paint();
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF cropRect = new RectF();
    private final RectF videoRect = new RectF();

    private CropAspectRatio aspectRatio = CropAspectRatio.FREE;

    private float handleTouchRadius;
    private float handleDrawRadius;

    private enum Handle {NONE, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER}

    private Handle activeHandle = Handle.NONE;
    private float lastTouchX;
    private float lastTouchY;

    @Nullable
    private OnCropChangedListener listener;

    @Nullable
    private CropInfo pendingCropInfo = null;

    public interface OnCropChangedListener {
        void onCropChanged(@NonNull CropInfo cropInfo);
    }

    public CropOverlayView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CropOverlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CropOverlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        handleTouchRadius = HANDLE_TOUCH_RADIUS_DP * density;
        handleDrawRadius = HANDLE_DRAW_RADIUS_DP * density;

        dimPaint.setColor(0x99000000);
        dimPaint.setStyle(Paint.Style.FILL);

        borderPaint.setColor(0xFFFFFFFF);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(BORDER_STROKE_DP * density);

        gridPaint.setColor(0x80FFFFFF);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(GRID_STROKE_DP * density);

        handlePaint.setColor(0xFFFFFFFF);
        handlePaint.setStyle(Paint.Style.FILL);
    }

    public void setOnCropChangedListener(@Nullable OnCropChangedListener listener) {
        this.listener = listener;
    }

    public void setVideoViewport(@NonNull RectF viewport) {
        boolean sizeChanged = !videoRect.isEmpty() && (viewport.width() != videoRect.width() || viewport.height() != videoRect.height());
        videoRect.set(viewport);
        if (cropRect.isEmpty()) {
            cropRect.set(videoRect);
        } else if (sizeChanged) {
            cropRect.set(videoRect);
        } else {
            constrainCropRect();
        }
        if (pendingCropInfo != null) {
            applyCropInfoToRect(pendingCropInfo);
            pendingCropInfo = null;
        }
        invalidate();
    }

    public void setCropInfo(@NonNull CropInfo cropInfo/*, @NonNull CropAspectRatio ratio*/) {
//        this.aspectRatio = ratio;
        if (videoRect.isEmpty()) {
            pendingCropInfo = cropInfo;
        } else {
            applyCropInfoToRect(cropInfo);
        }
    }

    private void applyCropInfoToRect(@NonNull CropInfo cropInfo) {
        if (videoRect.isEmpty()) return;
        float vw = videoRect.width();
        float vh = videoRect.height();
        cropRect.set(
                videoRect.left + cropInfo.getLeft() * vw,
                videoRect.top + cropInfo.getTop() * vh,
                videoRect.left + cropInfo.getRight() * vw,
                videoRect.top + cropInfo.getBottom() * vh
        );
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (videoRect.isEmpty() || cropRect.isEmpty()) return;

        drawDimOverlay(canvas);
        drawGrid(canvas);
        drawBorder(canvas);
        drawHandles(canvas);
    }

    private void drawDimOverlay(@NonNull Canvas canvas) {
        canvas.drawRect(0, 0, getWidth(), cropRect.top, dimPaint);
        canvas.drawRect(0, cropRect.bottom, getWidth(), getHeight(), dimPaint);
        canvas.drawRect(0, cropRect.top, cropRect.left, cropRect.bottom, dimPaint);
        canvas.drawRect(cropRect.right, cropRect.top, getWidth(), cropRect.bottom, dimPaint);
    }

    private void drawGrid(@NonNull Canvas canvas) {
        float cellW = cropRect.width() / GRID_COLUMNS;
        float cellH = cropRect.height() / GRID_ROWS;
        for (int i = 1; i < GRID_COLUMNS; i++) {
            float x = cropRect.left + i * cellW;
            canvas.drawLine(x, cropRect.top, x, cropRect.bottom, gridPaint);
        }
        for (int i = 1; i < GRID_ROWS; i++) {
            float y = cropRect.top + i * cellH;
            canvas.drawLine(cropRect.left, y, cropRect.right, y, gridPaint);
        }
    }

    private void drawBorder(@NonNull Canvas canvas) {
        canvas.drawRect(cropRect, borderPaint);
    }

    private void drawHandles(@NonNull Canvas canvas) {
        canvas.drawCircle(cropRect.left, cropRect.top, handleDrawRadius, handlePaint);
        canvas.drawCircle(cropRect.right, cropRect.top, handleDrawRadius, handlePaint);
        canvas.drawCircle(cropRect.left, cropRect.bottom, handleDrawRadius, handlePaint);
        canvas.drawCircle(cropRect.right, cropRect.bottom, handleDrawRadius, handlePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (videoRect.isEmpty()) return false;
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                activeHandle = hitTest(x, y);
                lastTouchX = x;
                lastTouchY = y;
                return activeHandle != Handle.NONE;
            case MotionEvent.ACTION_MOVE:
                if (activeHandle == Handle.NONE) return false;
                float dx = x - lastTouchX;
                float dy = y - lastTouchY;
                applyDelta(dx, dy);
                lastTouchX = x;
                lastTouchY = y;
                invalidate();
                notifyListener();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                activeHandle = Handle.NONE;
                return true;
        }
        return super.onTouchEvent(event);
    }

    private Handle hitTest(float x, float y) {
        if (dist(x, y, cropRect.left, cropRect.top) <= handleTouchRadius) return Handle.TOP_LEFT;
        if (dist(x, y, cropRect.right, cropRect.top) <= handleTouchRadius) return Handle.TOP_RIGHT;
        if (dist(x, y, cropRect.left, cropRect.bottom) <= handleTouchRadius)
            return Handle.BOTTOM_LEFT;
        if (dist(x, y, cropRect.right, cropRect.bottom) <= handleTouchRadius)
            return Handle.BOTTOM_RIGHT;
        if (cropRect.contains(x, y)) return Handle.CENTER;
        return Handle.NONE;
    }

    private float dist(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private void applyDelta(float dx, float dy) {
        float minW = videoRect.width() * MIN_CROP_FRACTION;
        float minH = videoRect.height() * MIN_CROP_FRACTION;

        switch (activeHandle) {
            case TOP_LEFT:
                cropRect.left = Math.min(cropRect.left + dx, cropRect.right - minW);
                cropRect.top = Math.min(cropRect.top + dy, cropRect.bottom - minH);
                if (aspectRatio.isFixed()) enforceAspectRatioFromTopLeft();
                break;
            case TOP_RIGHT:
                cropRect.right = Math.max(cropRect.right + dx, cropRect.left + minW);
                cropRect.top = Math.min(cropRect.top + dy, cropRect.bottom - minH);
                if (aspectRatio.isFixed()) enforceAspectRatioFromTopRight();
                break;
            case BOTTOM_LEFT:
                cropRect.left = Math.min(cropRect.left + dx, cropRect.right - minW);
                cropRect.bottom = Math.max(cropRect.bottom + dy, cropRect.top + minH);
                if (aspectRatio.isFixed()) enforceAspectRatioFromBottomLeft();
                break;
            case BOTTOM_RIGHT:
                cropRect.right = Math.max(cropRect.right + dx, cropRect.left + minW);
                cropRect.bottom = Math.max(cropRect.bottom + dy, cropRect.top + minH);
                if (aspectRatio.isFixed()) enforceAspectRatioFromBottomRight();
                break;
            case CENTER:
                float newLeft = cropRect.left + dx;
                float newTop = cropRect.top + dy;
                float w = cropRect.width();
                float h = cropRect.height();
                newLeft = Math.max(videoRect.left, Math.min(newLeft, videoRect.right - w));
                newTop = Math.max(videoRect.top, Math.min(newTop, videoRect.bottom - h));
                cropRect.offsetTo(newLeft, newTop);
                break;
            default:
                break;
        }
        constrainCropRect();
    }

    private void enforceAspectRatioFromTopLeft() {
        float w = cropRect.width();
        float h = w * aspectRatio.heightRatio / aspectRatio.widthRatio;
        cropRect.top = cropRect.bottom - h;
    }

    private void enforceAspectRatioFromTopRight() {
        float w = cropRect.width();
        float h = w * aspectRatio.heightRatio / aspectRatio.widthRatio;
        cropRect.top = cropRect.bottom - h;
    }

    private void enforceAspectRatioFromBottomLeft() {
        float w = cropRect.width();
        float h = w * aspectRatio.heightRatio / aspectRatio.widthRatio;
        cropRect.bottom = cropRect.top + h;
    }

    private void enforceAspectRatioFromBottomRight() {
        float w = cropRect.width();
        float h = w * aspectRatio.heightRatio / aspectRatio.widthRatio;
        cropRect.bottom = cropRect.top + h;
    }

    private void constrainCropRect() {
        cropRect.left = Math.max(cropRect.left, videoRect.left);
        cropRect.top = Math.max(cropRect.top, videoRect.top);
        cropRect.right = Math.min(cropRect.right, videoRect.right);
        cropRect.bottom = Math.min(cropRect.bottom, videoRect.bottom);
    }

    public void setAspectRatio(@NonNull CropAspectRatio ratio) {
        if (videoRect.isEmpty()) {
            return;
        }
        this.aspectRatio = ratio;
        if (ratio.isFixed()) {
            float centerX = videoRect.centerX();
            float centerY = videoRect.centerY();
            float maxW = videoRect.width();
            float maxH = videoRect.height();
            float targetW, targetH;
            if (maxW / ratio.widthRatio <= maxH / ratio.heightRatio) {
                targetW = maxW;
                targetH = targetW * ratio.heightRatio / ratio.widthRatio;
            } else {
                targetH = maxH;
                targetW = targetH * ratio.widthRatio / ratio.heightRatio;
            }
            cropRect.set(
                    centerX - targetW / 2f,
                    centerY - targetH / 2f,
                    centerX + targetW / 2f,
                    centerY + targetH / 2f
            );
            constrainCropRect();
        } else {
            cropRect.set(videoRect);
        }
        invalidate();
        notifyListener();
    }

    @NonNull
    public CropInfo getCurrentCropInfo() {
        if (videoRect.isEmpty() || cropRect.isEmpty()) return CropInfo.NONE;
        float vw = videoRect.width();
        float vh = videoRect.height();
        float left = (cropRect.left - videoRect.left) / vw;
        float top = (cropRect.top - videoRect.top) / vh;
        float right = (cropRect.right - videoRect.left) / vw;
        float bottom = (cropRect.bottom - videoRect.top) / vh;
        left = Math.max(0f, Math.min(1f, left));
        top = Math.max(0f, Math.min(1f, top));
        right = Math.max(0f, Math.min(1f, right));
        bottom = Math.max(0f, Math.min(1f, bottom));
        if (left == 0f && top == 0f && right == 1f && bottom == 1f) return CropInfo.NONE;
        return new CropInfo(left, top, right, bottom);
    }

    private void notifyListener() {
        if (listener != null) {
            listener.onCropChanged(getCurrentCropInfo());
        }
    }
}

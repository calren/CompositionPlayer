package com.example.compositionplayer;

import static androidx.media3.common.util.Assertions.checkNotNull;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import androidx.media3.common.VideoFrameProcessingException;
import androidx.media3.common.util.NonNullApi;
import androidx.media3.common.util.Size;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.effect.BitmapOverlay;

import org.jetbrains.annotations.NotNull;

@UnstableApi
public abstract class CanvasOverlay extends BitmapOverlay {
    private final boolean useInputFrameSize;

    private @NotNull Bitmap lastBitmap;
    private @NotNull Canvas lastCanvas;
    private volatile int width;
    private volatile int height;

    /**
     * Creates a new {@code CanvasOverlay}.
     *
     * @param useInputFrameSize Whether to create the {@link Canvas} to match the input frame size, if
     *     {@code false}, {@link #setCanvasSize(int, int)} must be set before the first invocation to
     *     {@link #onDraw}.
     */
    public CanvasOverlay(boolean useInputFrameSize) {
        this.useInputFrameSize = useInputFrameSize;
    }

    /**
     * Perform custom drawing onto the {@link Canvas}.
     *
     * @param canvas The {@link Canvas} to draw onto.
     * @param presentationTimeUs The presentation timestamp, in microseconds.
     */
    public abstract void onDraw(Canvas canvas, long presentationTimeUs);

    /**
     * Sets the size of the {@link Canvas}.
     *
     * <p>The default canvas size will be of the same size as the video frame.
     *
     * <p>The size will be applied on the next invocation of {@link #onDraw}.
     */
    public void setCanvasSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void configure(Size videoSize) {
        super.configure(videoSize);
        if (useInputFrameSize) {
            setCanvasSize(videoSize.getWidth(), videoSize.getHeight());
        }
    }

    @Override
    public Bitmap getBitmap(long presentationTimeUs) {
        if (lastBitmap == null || lastBitmap.getWidth() != width || lastBitmap.getHeight() != height) {
            lastBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            lastCanvas = new Canvas(lastBitmap);
        }
        onDraw(checkNotNull(lastCanvas), presentationTimeUs);
        return lastBitmap;
    }

    @Override
    public void release() throws VideoFrameProcessingException {
        super.release();
        if (lastBitmap != null) {
            lastBitmap.recycle();
        }
    }
}

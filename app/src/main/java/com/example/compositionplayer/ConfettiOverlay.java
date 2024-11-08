package com.example.compositionplayer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.media3.common.VideoFrameProcessingException;
import androidx.media3.common.util.Size;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;

import com.airbnb.lottie.LottieDrawable;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Mimics an emitter of confetti, dropping from the center of the frame. */
/* package */ @UnstableApi
final class ConfettiOverlay extends CanvasOverlay {

    private static final ImmutableList<String> CONFETTI_TEXTS =
            ImmutableList.of("❊", "✿", "❊", "✦︎", "♥︎", "☕︎");
    private static final int EMITTER_POSITION_Y = -50;
    private static final int CONFETTI_BASE_SIZE = 30;
    private static final int CONFETTI_SIZE_VARIATION = 10;

    private final List<Confetti> confettiList;
    private final Random random;
    private final Paint paint;
    private final Handler handler;
    @Nullable private Runnable runnable;
    private int width;
    private int height;
    private boolean started;

    public ConfettiOverlay() {
        super(true);
        confettiList = new ArrayList<>();
        random = new Random();
        paint = new Paint();
        paint.setAntiAlias(true);
        handler = new Handler(Util.getCurrentOrMainLooper());
    }

    @Override
    public void configure(Size videoSize) {
        super.configure(videoSize);
        this.width = videoSize.getWidth();
        this.height = videoSize.getHeight();
    }

    @Override
    public synchronized void onDraw(Canvas canvas, long presentationTimeUs) {
        Log.i("Caren", "confetti on draw");
        if (!started) {
            start();
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        for (int i = 0; i < confettiList.size(); i++) {
            Confetti confetti = confettiList.get(i);
            if (confetti.y > (float) height / 2 || confetti.x <= 0 || confetti.x > width) {
                confettiList.remove(confetti);
                continue;
            }
            confetti.draw(canvas, paint);
            confetti.update();
        }
    }

    /** Starts the confetti. */
    public void start() {
        runnable = this::addConfetti;
        handler.post(runnable);
        started = true;
    }

    /** Stops the confetti. */
    public void stop() {
//        checkStateNotNull(runnable);
        handler.removeCallbacks(runnable);
        confettiList.clear();
        started = false;
        runnable = null;
    }

    @Override
    public void release() throws VideoFrameProcessingException {
        super.release();
        handler.post(this::stop);
    }

    private synchronized void addConfetti() {
        for (int i = 0; i < 5; i++) {
            confettiList.add(
                    new Confetti(
                            CONFETTI_TEXTS.get(Math.abs(random.nextInt()) % CONFETTI_TEXTS.size()),
                            random,
                            /* x= */ (float) width / 2,
                            /* y= */ EMITTER_POSITION_Y,
                            /* size= */ CONFETTI_BASE_SIZE + random.nextInt(CONFETTI_SIZE_VARIATION),
                            /* color= */ Color.HSVToColor(
                            new float[] {
                                    /* hue= */ random.nextInt(360), /* saturation= */ 0.6f, /* value= */ 0.8f
                            })));
        }
        handler.postDelayed(this::addConfetti, /* delayMillis= */ 100);
    }

    private static final class Confetti {
        private final String text;
        private final float speedX;
        private final float speedY;
        private final int size;
        private final int color;

        private float x;
        private float y;

        public Confetti(String text, Random random, float x, float y, int size, int color) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
            speedX = 4 * (random.nextFloat() * 2 - 1); // Random speed in x direction
            speedY = 4 * random.nextFloat(); // Random downward speed
        }

        /** Draws the {@code Confetti} on the {@link Canvas}. */
        public void draw(Canvas canvas, Paint paint) {
            canvas.save();
            paint.setColor(color);
            paint.setTextSize(size);
            canvas.drawText(text, x, y, paint);
            canvas.restore();
        }

        /** Updates the {@code Confetti}. */
        public void update() {
            x += speedX;
            y += speedY;
        }
    }
}

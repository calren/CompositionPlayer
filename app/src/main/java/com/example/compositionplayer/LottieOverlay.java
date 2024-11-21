package com.example.compositionplayer;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.VideoFrameProcessingException;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.Size;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieCompositionFactory;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieListener;
import com.airbnb.lottie.LottieResult;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Mimics an emitter of confetti, dropping from the center of the frame.
 */
/* package */
@UnstableApi
final class LottieOverlay extends CanvasOverlay {
    @Nullable
    private Runnable runnable;
    private int width;
    private int height;
    private  Paint paint;
    private boolean started;
    private  Handler handler;

    LottieDrawable ld;

    public LottieOverlay(Context context) {
        super(true);
        ld = new LottieDrawable();
        try {
            Log.i("Caren", "Loading lottie overlay");
            ld.setImagesAssetsFolder("images2/");
            LottieCompositionFactory.fromRawRes(context, R.raw.anim).addListener(result -> ld.setComposition(result));
            paint = new Paint();
            paint.setAntiAlias(true);
            handler = new Handler(Util.getCurrentOrMainLooper());
            Log.i("Caren", "Finished loading lottie overlay");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void configure(Size videoSize) {
        super.configure(videoSize);
        this.width = videoSize.getWidth();
        this.height = videoSize.getHeight();
    }

    @Override
    public synchronized void onDraw(Canvas canvas, long presentationTimeUs) {
//        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        ld.addAnimatorUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                ld.draw(canvas);
            }
        });

        ld.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {

            }
        });
        handler.post(() -> {
//                while (ld.isAnimating()) {
//                while (ld.isRunning()) {
//                    Log.i("Caren", "lottie on draw");
//                    ld.draw(canvas);
//                    Log.i("Caren", "frame: " + ld.getFrame());
//                }

            if (!ld.isRunning()) {
                ld.playAnimation();
            }
        });
    }

}

package com.example.compositionplayer

import android.animation.Animator.AnimatorListener
import android.media.Image
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Assertions
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.RgbFilter
import androidx.media3.effect.SpeedChangeEffect
import androidx.media3.transformer.Composition
import androidx.media3.transformer.CompositionPlayer
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.ui.PlayerView
import androidx.navigation.ui.AppBarConfiguration
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.example.compositionplayer.databinding.ActivityMainBinding
import com.google.android.material.chip.Chip

@UnstableApi
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private var previewPlayerView: PlayerView? = null
    private var compositionPlayer: CompositionPlayer? = null

    private var grayScaleChip: Chip? = null
    private var dizzyCropChip: Chip? = null
    private var fastSpeedChip: Chip? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

//        val ld = LottieDrawable()
//        ld.setImagesAssetsFolder("images/")
//        LottieCompositionFactory.fromRawRes(this, R.raw.smiles_w_name).addListener { result ->
//            ld.setComposition(
//                result
//            )
//        }
//
//        findViewById<ImageView>(R.id.image_view).setImageDrawable(ld)
//        ld.start()

        previewPlayerView = findViewById(R.id.composition_player_view)
        grayScaleChip = findViewById(R.id.grayscale_chip)
        dizzyCropChip = findViewById(R.id.dizzy_crop_chip)
        fastSpeedChip = findViewById(R.id.fast_speed_chip)

        val mediaItem = MediaItem.fromUri("")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        prepareCompositionPlayer()

        val startingComposition =
            createCompositionFromMedia("https://videocdn.cdnpk.net/videos/1b54e0c4-3a03-530f-b1a3-1c94d5c19f2c/horizontal/previews/clear/small.mp4?token=exp=1732217229~hmac=b72e4cc4a38aa7ebc3bd9b006ecc1cf1275aa0f90fb50f3ad4a699156bd6c830")
        playCompositionPreview(startingComposition)

        val originalMediaItem = MediaItem.Builder()
            .setUri("https://videocdn.cdnpk.net/videos/1b54e0c4-3a03-530f-b1a3-1c94d5c19f2c/horizontal/previews/clear/small.mp4?token=exp=1732217229~hmac=b72e4cc4a38aa7ebc3bd9b006ecc1cf1275aa0f90fb50f3ad4a699156bd6c830")
            .build()

        grayScaleChip?.setOnCheckedChangeListener { _, isChecked ->

            val editedMediaItem = EditedMediaItem.Builder(originalMediaItem)
                .setDurationUs(15_000_000L) // This is weird, why is this needed
                .setEffects(
                    Effects(
                    listOf(),
                    getSelectedVideoEffectsList())
                )
                .build()
            val editedMediaItemSequence = EditedMediaItemSequence(editedMediaItem)
            val composition = Composition.Builder(editedMediaItemSequence).build()

            playCompositionPreview(composition)

        }

        dizzyCropChip?.setOnCheckedChangeListener { _, isChecked ->

            val editedMediaItem = EditedMediaItem.Builder(originalMediaItem)
                .setDurationUs(15_000_000L) // This is weird, why is this needed
                .setEffects(Effects(
                    listOf(),
                    getSelectedVideoEffectsList()
                ))
                .build()
            val editedMediaItemSequence = EditedMediaItemSequence(editedMediaItem)
            val composition = Composition.Builder(editedMediaItemSequence).build()

            playCompositionPreview(composition)
        }

        fastSpeedChip?.setOnCheckedChangeListener { _, isChecked ->

            val editedMediaItem = EditedMediaItem.Builder(originalMediaItem)
                .setDurationUs(15_000_000L) // This is weird, why is this needed
                .setEffects(Effects(
                    listOf(),
                    getSelectedVideoEffectsList()
                ))
                .build()
            val editedMediaItemSequence = EditedMediaItemSequence(editedMediaItem)
            val composition = Composition.Builder(editedMediaItemSequence).build()

            playCompositionPreview(composition)
        }


    }

    private fun getSelectedVideoEffectsList(): List<Effect> {
        val videoEffects = mutableListOf<Effect>()
        if (grayScaleChip?.isChecked == true) {
            videoEffects.add(RgbFilter.createGrayscaleFilter())
        }
        if (dizzyCropChip?.isChecked == true) {
//            videoEffects.add(MatrixTransformationFactory.createDizzyCropEffect())
            videoEffects.add(OverlayEffect(listOf(LottieOverlay(this))))
        }
        if (fastSpeedChip?.isChecked == true) {
            videoEffects.add(SpeedChangeEffect(5f))
        }
        return videoEffects
    }

    override fun onStart() {
        super.onStart()
        previewPlayerView?.onResume()
        Assertions.checkStateNotNull(previewPlayerView).onResume()
    }

    override fun onStop() {
        super.onStop()
        previewPlayerView?.onPause()
        releasePlayer()
    }

    private fun createCompositionFromMedia(uri: String): Composition {
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .build()
        val editedMediaItem = EditedMediaItem.Builder(mediaItem)
            .setDurationUs(2_000_000L) // This is weird
            .build()
        val editedMediaItemSequence = EditedMediaItemSequence(editedMediaItem)
        return Composition.Builder(editedMediaItemSequence).build()
    }

    private fun prepareCompositionPlayer() {
        compositionPlayer = CompositionPlayer.Builder(applicationContext).build()
        previewPlayerView?.player = compositionPlayer
        previewPlayerView?.controllerAutoShow = false
        // Add listener with onPlayerError?
    }

    private fun playCompositionPreview(composition: Composition) {
        releasePlayer()
        prepareCompositionPlayer()

        compositionPlayer?.setComposition(composition)
        compositionPlayer?.prepare()
        compositionPlayer?.play()
    }

    private fun releasePlayer() {
        compositionPlayer?.release()
        compositionPlayer = null
    }
}
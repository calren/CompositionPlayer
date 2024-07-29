package com.example.compositionplayer

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Assertions
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.RgbFilter
import androidx.media3.effect.SpeedChangeEffect
import androidx.media3.transformer.Composition
import androidx.media3.transformer.CompositionPlayer
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.ui.PlayerView
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

        previewPlayerView = findViewById(R.id.composition_player_view)
        grayScaleChip = findViewById(R.id.grayscale_chip)
        dizzyCropChip = findViewById(R.id.dizzy_crop_chip)
        fastSpeedChip = findViewById(R.id.fast_speed_chip)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        prepareCompositionPlayer()

        val startingComposition =
            createCompositionFromMedia("https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4")
        playCompositionPreview(startingComposition)

        val originalMediaItem = MediaItem.Builder()
            .setUri("https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4")
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
            videoEffects.add(MatrixTransformationFactory.createDizzyCropEffect())
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
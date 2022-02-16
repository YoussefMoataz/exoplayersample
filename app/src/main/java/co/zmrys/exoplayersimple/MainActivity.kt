package co.zmrys.exoplayersimple

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.ResultReceiver
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.appcompat.app.AppCompatActivity
import co.zmrys.exoplayersimple.databinding.ActivityMainBinding
import co.zmrys.exoplayersimple.extensions.displayTitle
import co.zmrys.exoplayersimple.extensions.title
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "main"
        const val MEDIA_SESSION_TOKEN = "media_session_token"
    }

    //Service
    private var playerService: PlayerService? = null

    private var _binding: ActivityMainBinding? = null

    private val binding: ActivityMainBinding get() = _binding!!

    private val progressiveSourceFactory by lazy {
        ProgressiveMediaSource.Factory(
            DefaultDataSourceFactory(
                this,
                Util.getUserAgent(this, "Progressive")
            )
        )
    }

    private val mediaSession: MediaSessionCompat by lazy {
        MediaSessionCompat(this, TAG)
    }

    private val data: List<MediaMetadataCompat> by lazy {
        Utils.createSimpleData()
    }
    private lateinit var dataa: MediaMetadataCompat

    private val exoPlayer by lazy {
        Utils.createSimpleExoPlayer(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playerView.player = exoPlayer
        preparePlayer()

        val mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.apply {
            setPlayer(exoPlayer)
            setQueueNavigator(createQueueNavigator())
//            setQueueEditor(createQueueEditor())
        }

        val mediaController = MediaControllerCompat(this, mediaSession)
        MediaControllerCompat.setMediaController(this, mediaController)
        val transportControls = MediaControllerCompat.getMediaController(this).transportControls
        MediaControllerCompat.getMediaController(this)

        binding.btnPlay.setOnClickListener {
            transportControls.play()
        }

        binding.btnPause.setOnClickListener {
            transportControls.pause()
        }

    }

    private fun createQueueEditor() = object : MediaSessionConnector.QueueEditor {
        override fun onCommand(
            player: Player,
            command: String,
            extras: Bundle?,
            cb: ResultReceiver?
        ): Boolean {
            TODO("Not yet implemented")
        }

        override fun onAddQueueItem(player: Player, description: MediaDescriptionCompat) {
            TODO("Not yet implemented")
        }

        override fun onAddQueueItem(
            player: Player,
            description: MediaDescriptionCompat,
            index: Int
        ) {
            TODO("Not yet implemented")
        }

        override fun onRemoveQueueItem(player: Player, description: MediaDescriptionCompat) {
            TODO("Not yet implemented")
        }

    }

    private fun createQueueNavigator() = object : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return MediaDescriptionCompat.Builder()
                .setTitle(dataa.displayTitle)
                .build()
        }

        override fun onSkipToPrevious(player: Player) {
            Timber.i("onSkipToPrevious")
        }
    }

    private fun preparePlayer() {
        dataa = MediaMetadataCompat.Builder()
            .apply {
                title = "fatiha"
                displayTitle = "fatiha"
            }.build()
        exoPlayer.setMediaItem(MediaItem.fromUri("https://server8.mp3quran.net/ahmad_huth/002.mp3"))
        exoPlayer.prepare()
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, PlayerService::class.java)
        intent.putExtra(MEDIA_SESSION_TOKEN, mediaSession.sessionToken)
        bindService(intent, playerServiceConnection, Service.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
        if (playerService != null) {
            unbindService(playerServiceConnection)
        }
    }

    override fun onStop() {
        super.onStop()
//        if (playerService != null) {
//            unbindService(playerServiceConnection)
//        }
    }

    private val playerServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PlayerService.PlayerServiceBinder
            playerService = binder.service
            playerService?.setPlayer(exoPlayer)
            Timber.i("Connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Timber.i("Disconnected")
            playerService = null
        }

    }
}
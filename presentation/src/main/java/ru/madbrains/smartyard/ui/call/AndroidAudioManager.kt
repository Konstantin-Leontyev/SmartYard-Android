package ru.madbrains.smartyard.ui.call

import android.content.Context
import android.media.AudioManager
import android.os.Vibrator

/**
 * @author Nail Shakurov
 * Created on 23.07.2020.
 */
class AndroidAudioManager(var context: Context) {

    private var mAudioManager: AudioManager? = null
        private set
    var vibrator: Vibrator? = null
        private set

    init {
        mAudioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun routeAudioToEarPiece() {
        routeAudioToSpeakerHelper(false)
    }

    fun routeAudioToSpeaker() {
        routeAudioToSpeakerHelper(true)
    }

    private fun routeAudioToSpeakerHelper(speakerOn: Boolean) {
        mAudioManager?.isSpeakerphoneOn = speakerOn
    }
}

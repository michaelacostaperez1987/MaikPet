package com.example.pet.util

import android.content.Context
import android.media.MediaPlayer
import com.example.pet.R

object SoundPlayer {
    private var mediaPlayer: MediaPlayer? = null
    
    fun playDogBark(context: Context) {
        playSound(context, R.raw.bark)
    }
    
    fun playCatMeow(context: Context) {
        playSound(context, R.raw.meow)
    }
    
    private fun playSound(context: Context, soundResId: Int) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, soundResId)
            mediaPlayer?.setOnCompletionListener { mp ->
                mp.release()
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

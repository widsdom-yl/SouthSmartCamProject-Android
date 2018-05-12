package stcam.stcamproject.Util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

import stcam.stcamproject.R;

/**
 * Created by gyl1 on 3/30/17.
 */


public class PlayVoice {
    public enum EnumSoundWav {
        CLICK,SNAP
    }
    public static MediaPlayer mp;  //“Ù∆µ≤•∑≈

    /**
     *
     * @param context
     * @param wav
     */
    public static void playClickVoice(Context context,EnumSoundWav wav){
        if (true) {
            try {
                if (mp == null) {
                    mp = new MediaPlayer();
                }
                mp.reset();
                AssetFileDescriptor afd = null;
                if (wav == EnumSoundWav.CLICK){

                    afd = context.getResources().openRawResourceFd(R.raw.soundkeypress);
                }
                else if (wav == EnumSoundWav.SNAP){
                    afd = context.getResources().openRawResourceFd(R.raw.soundsnapshot);
                }

                if (afd == null)  return;
                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mp.prepare();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mp.start();
        }
    }
}

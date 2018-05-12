package stcam.stcamproject.View;

import android.content.Context;
import android.util.AttributeSet;

import stcam.stcamproject.Util.PlayVoice;

/**
 * Created by gyl1 on 3/30/17.
 */

public class VoiceImageButton extends android.support.v7.widget.AppCompatImageButton {
    PlayVoice.EnumSoundWav mWav;
    public VoiceImageButton(Context context) {
        super(context);
    }
    public VoiceImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public VoiceImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public void setEnumSoundWav(PlayVoice.EnumSoundWav wav){
        mWav = wav;
    }
    @Override
    public boolean performClick() {
        //调用发声方法
        PlayVoice.playClickVoice(getContext(),mWav);
        return super.performClick();
    }
}

package de.nulide.findmydevice.utils;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import de.nulide.findmydevice.ui.RingerActivity;

public class Ringer {

    public static void ring(Context context, int duration){
        Intent ringerActivity = new Intent(context, RingerActivity.class);
        ringerActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ringerActivity.putExtra(RingerActivity.RING_DURATION, duration);
        context.startActivity(ringerActivity);
    }

    public static Ringtone getRingtone(Context context, String ringtone) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
        if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL)
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

        Ringtone r = RingtoneManager.getRingtone(context, Uri.parse(ringtone));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes aa = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .build();
            r.setAudioAttributes(aa);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            r.setLooping(true);
        }
        return r;
    }

    public static String getDefaultRingtoneAsString(){
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString();
    }

}

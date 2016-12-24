package com.wizardsofm.deskclock.settings;

import android.content.Context;
import android.media.AudioManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by madushaperera on 12/14/16.
 */

public class VoiceCommandPreference  extends Preference {

    private EditText snoozeCommand;
    private EditText stopCommand;



    public VoiceCommandPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        // Disable click feedback for this preference.
        holder.itemView.setClickable(false);

        final Context context = getContext();
        final AudioManager audioManager =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        snoozeCommand = (EditText) holder.findViewById(com.wizardsofm.deskclock.R.id.snooze_command);
        stopCommand = (EditText) holder.findViewById(com.wizardsofm.deskclock.R.id.stop_command);
//        updateIcon();

//        final ContentObserver volumeObserver = new ContentObserver(mSeekbar.getHandler()) {
//            @Override
//            public void onChange(boolean selfChange) {
//                // Volume was changed elsewhere, update our slider.
//                mSeekbar.setProgress(audioManager.getStreamVolume(
//                        AudioManager.STREAM_ALARM));
//            }
//        };
//
//        mSeekbar.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
//            @Override
//            public void onViewAttachedToWindow(View v) {
//                context.getContentResolver().registerContentObserver(Settings.System.CONTENT_URI,
//                        true, volumeObserver);
//            }
//
//            @Override
//            public void onViewDetachedFromWindow(View v) {
//                context.getContentResolver().unregisterContentObserver(volumeObserver);
//            }
//        });
//
//
//        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if (fromUser) {
//                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, progress, 0);
//                }
//                updateIcon();
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                if (!mPreviewPlaying && seekBar.getProgress() != 0) {
//                    // If we are not currently playing and progress is set to non-zero, start.
//                    RingtonePreviewKlaxon.start(
//                            context, DataModel.getDataModel().getDefaultAlarmRingtoneUri());
//                    mPreviewPlaying = true;
//                    seekBar.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            RingtonePreviewKlaxon.stop(context);
//                            mPreviewPlaying = false;
//                        }
//                    }, ALARM_PREVIEW_DURATION_MS);
//                }
//            }
//        });
    }


}

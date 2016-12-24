/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wizardsofm.deskclock.data;

import android.app.Service;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringRes;

import com.wizardsofm.deskclock.Utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * All application-wide data is accessible through this singleton.
 */
public final class DataModel {

    /** Indicates the display style of clocks. */
    public enum ClockStyle {ANALOG, DIGITAL}

    /** Indicates the preferred sort order of cities. */
    public enum CitySort {NAME, UTC_OFFSET}

    public static final String ACTION_WORLD_CITIES_CHANGED =
            "com.android.deskclock.WORLD_CITIES_CHANGED";

    /** The single instance of this data model that exists for the life of the application. */
    private static final DataModel sDataModel = new DataModel();

    private Handler mHandler;

    private Context mContext;

    /** The model from which settings are fetched. */
    private SettingsModel mSettingsModel;

    /** The model from which city data are fetched. */
    private CityModel mCityModel;

    /** The model from which timer data are fetched. */
    private TimerModel mTimerModel;

    /** The model from which alarm data are fetched. */
    private AlarmModel mAlarmModel;

    /** The model from which widget data are fetched. */
    private WidgetModel mWidgetModel;

    /** The model from which stopwatch data are fetched. */
    private StopwatchModel mStopwatchModel;

    /** The model from which notification data are fetched. */
    private NotificationModel mNotificationModel;

    public static DataModel getDataModel() {
        return sDataModel;
    }

    private DataModel() {}

    /**
     * The context may be set precisely once during the application life.
     */
    public void setContext(Context context) {
        if (mContext != null) {
            throw new IllegalStateException("context has already been set");
        }
        mContext = context.getApplicationContext();

        mSettingsModel = new SettingsModel(mContext);
        mNotificationModel = new NotificationModel();
        mCityModel = new CityModel(mContext, mSettingsModel);
        mWidgetModel = new WidgetModel(mContext);
        mAlarmModel = new AlarmModel(mContext, mSettingsModel);
        mStopwatchModel = new StopwatchModel(mContext, mNotificationModel);
        mTimerModel = new TimerModel(mContext, mSettingsModel, mNotificationModel);
    }

    /**
     * Convenience for {@code run(runnable, 0)}, i.e. waits indefinitely.
     */
    public void run(Runnable runnable) {
        try {
            run(runnable, 0 /* waitMillis */);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Posts a runnable to the main thread and blocks until the runnable executes. Used to access
     * the data model from the main thread.
     */
    public void run(Runnable runnable, long waitMillis) throws InterruptedException {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
            return;
        }

        final ExecutedRunnable er = new ExecutedRunnable(runnable);
        getHandler().post(er);

        // Wait for the data to arrive, if it has not.
        synchronized (er) {
            if (!er.isExecuted()) {
                er.wait(waitMillis);
            }
        }
    }

    /**
     * @return a handler associated with the main thread
     */
    private synchronized Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

    //
    // Application
    //

    /**
     * @param inForeground {@code true} to indicate the application is open in the foreground
     */
    public void setApplicationInForeground(boolean inForeground) {
        Utils.enforceMainLooper();

        if (mNotificationModel.isApplicationInForeground() != inForeground) {
            mNotificationModel.setApplicationInForeground(inForeground);

            // Refresh all notifications in response to a change in app open state.
            mTimerModel.updateNotification();
            mStopwatchModel.updateNotification();
        }
    }

    /**
     * @return {@code true} when the application is open in the foreground; {@code false} otherwise
     */
    public boolean isApplicationInForeground() {
        Utils.enforceMainLooper();
        return mNotificationModel.isApplicationInForeground();
    }

    /**
     * Called when the notifications may be stale or absent from the notification manager and must
     * be rebuilt. e.g. after upgrading the application
     */
    public void updateAllNotifications() {
        Utils.enforceMainLooper();
        mTimerModel.updateNotification();
        mStopwatchModel.updateNotification();
    }

    //
    // Cities
    //

    /**
     * @return a list of all cities in their display order
     */
    public List<City> getAllCities() {
        Utils.enforceMainLooper();
        return mCityModel.getAllCities();
    }

    /**
     * @param cityName the case-insensitive city name to search for
     * @return the city with the given {@code cityName}; {@code null} if no such city exists
     */
    public City getCity(String cityName) {
        Utils.enforceMainLooper();
        return mCityModel.getCity(cityName);
    }

    /**
     * @return a city representing the user's home timezone
     */
    public City getHomeCity() {
        Utils.enforceMainLooper();
        return mCityModel.getHomeCity();
    }

    /**
     * @return a list of cities not selected for display
     */
    public List<City> getUnselectedCities() {
        Utils.enforceMainLooper();
        return mCityModel.getUnselectedCities();
    }

    /**
     * @return a list of cities selected for display
     */
    public List<City> getSelectedCities() {
        Utils.enforceMainLooper();
        return mCityModel.getSelectedCities();
    }

    /**
     * @param cities the new collection of cities selected for display by the user
     */
    public void setSelectedCities(Collection<City> cities) {
        Utils.enforceMainLooper();
        mCityModel.setSelectedCities(cities);
    }

    /**
     * @return a comparator used to locate index positions
     */
    public Comparator<City> getCityIndexComparator() {
        Utils.enforceMainLooper();
        return mCityModel.getCityIndexComparator();
    }

    /**
     * @return the order in which cities are sorted
     */
    public CitySort getCitySort() {
        Utils.enforceMainLooper();
        return mCityModel.getCitySort();
    }

    /**
     * Adjust the order in which cities are sorted.
     */
    public void toggleCitySort() {
        Utils.enforceMainLooper();
        mCityModel.toggleCitySort();
    }

    //
    // Timers
    //

    /**
     * @param timerListener to be notified when timers are added, updated and removed
     */
    public void addTimerListener(TimerListener timerListener) {
        Utils.enforceMainLooper();
        mTimerModel.addTimerListener(timerListener);
    }

    /**
     * @param timerListener to no longer be notified when timers are added, updated and removed
     */
    public void removeTimerListener(TimerListener timerListener) {
        Utils.enforceMainLooper();
        mTimerModel.removeTimerListener(timerListener);
    }

    /**
     * @return a list of timers for display
     */
    public List<Timer> getTimers() {
        Utils.enforceMainLooper();
        return mTimerModel.getTimers();
    }

    /**
     * @return a list of expired timers for display
     */
    public List<Timer> getExpiredTimers() {
        Utils.enforceMainLooper();
        return mTimerModel.getExpiredTimers();
    }

    /**
     * @param timerId identifies the timer to return
     * @return the timer with the given {@code timerId}
     */
    public Timer getTimer(int timerId) {
        Utils.enforceMainLooper();
        return mTimerModel.getTimer(timerId);
    }

    /**
     * @return the timer that last expired and is still expired now; {@code null} if no timers are
     *      expired
     */
    public Timer getMostRecentExpiredTimer() {
        Utils.enforceMainLooper();
        return mTimerModel.getMostRecentExpiredTimer();
    }

    /**
     * @param length the length of the timer in milliseconds
     * @param label describes the purpose of the timer
     * @param deleteAfterUse {@code true} indicates the timer should be deleted when it is reset
     * @return the newly added timer
     */
    public Timer addTimer(long length, String label, boolean deleteAfterUse) {
        Utils.enforceMainLooper();
        return mTimerModel.addTimer(length, label, deleteAfterUse);
    }

    /**
     * @param timer the timer to be removed
     */
    public void removeTimer(Timer timer) {
        Utils.enforceMainLooper();
        mTimerModel.removeTimer(timer);
    }

    /**
     * @param timer the timer to be started
     */
    public void startTimer(Timer timer) {
        Utils.enforceMainLooper();
        mTimerModel.updateTimer(timer.start());
    }

    /**
     * @param timer the timer to be paused
     */
    public void pauseTimer(Timer timer) {
        Utils.enforceMainLooper();
        mTimerModel.updateTimer(timer.pause());
    }

    /**
     * @param service used to start foreground notifications for expired timers
     * @param timer the timer to be expired
     */
    public void expireTimer(Service service, Timer timer) {
        Utils.enforceMainLooper();
        mTimerModel.expireTimer(service, timer);
    }

    /**
     * If the given {@code timer} is expired and marked for deletion after use then this method
     * removes the the timer. The timer is otherwise transitioned to the reset state and continues
     * to exist.
     *
     * @param timer the timer to be reset
     * @param eventLabelId the label of the timer event to send; 0 if no event should be sent
     * @return the reset {@code timer} or {@code null} if the timer was deleted
     */
    public Timer resetOrDeleteTimer(Timer timer, @StringRes int eventLabelId) {
        Utils.enforceMainLooper();
        return mTimerModel.resetOrDeleteTimer(timer, eventLabelId);
    }

    /**
     * Resets all timers.
     *
     * @param eventLabelId the label of the timer event to send; 0 if no event should be sent
     */
    public void resetTimers(@StringRes int eventLabelId) {
        Utils.enforceMainLooper();
        mTimerModel.resetTimers(eventLabelId);
    }

    /**
     * Resets all expired timers.
     *
     * @param eventLabelId the label of the timer event to send; 0 if no event should be sent
     */
    public void resetExpiredTimers(@StringRes int eventLabelId) {
        Utils.enforceMainLooper();
        mTimerModel.resetExpiredTimers(eventLabelId);
    }

    /**
     * Resets all unexpired timers.
     *
     * @param eventLabelId the label of the timer event to send; 0 if no event should be sent
     */
    public void resetUnexpiredTimers(@StringRes int eventLabelId) {
        Utils.enforceMainLooper();
        mTimerModel.resetUnexpiredTimers(eventLabelId);
    }

    /**
     * @param timer the timer to which a minute should be added to the remaining time
     */
    public void addTimerMinute(Timer timer) {
        Utils.enforceMainLooper();
        mTimerModel.updateTimer(timer.addMinute());
    }

    /**
     * @param timer the timer to which the new {@code label} belongs
     * @param label the new label to store for the {@code timer}
     */
    public void setTimerLabel(Timer timer, String label) {
        Utils.enforceMainLooper();
        mTimerModel.updateTimer(timer.setLabel(label));
    }

    /**
     * Updates the timer notifications to be current.
     */
    public void updateTimerNotification() {
        Utils.enforceMainLooper();
        mTimerModel.updateNotification();
    }

    /**
     * @return the uri of the default ringtone to play for all timers when no user selection exists
     */
    public Uri getDefaultTimerRingtoneUri() {
        Utils.enforceMainLooper();
        return mTimerModel.getDefaultTimerRingtoneUri();
    }

    /**
     * @return {@code true} iff the ringtone to play for all timers is the silent ringtone
     */
    public boolean isTimerRingtoneSilent() {
        Utils.enforceMainLooper();
        return mTimerModel.isTimerRingtoneSilent();
    }

    /**
     * @return the uri of the ringtone to play for all timers
     */
    public Uri getTimerRingtoneUri() {
        Utils.enforceMainLooper();
        return mTimerModel.getTimerRingtoneUri();
    }

    /**
     * @param uri the uri of the ringtone to play for all timers
     */
    public void setTimerRingtoneUri(Uri uri) {
        Utils.enforceMainLooper();
        mTimerModel.setTimerRingtoneUri(uri);
    }

    /**
     * @return the title of the ringtone that is played for all timers
     */
    public String getTimerRingtoneTitle() {
        Utils.enforceMainLooper();
        return mTimerModel.getTimerRingtoneTitle();
    }

    /**
     * @return whether vibrate is enabled for all timers.
     */
    public boolean getTimerVibrate() {
        Utils.enforceMainLooper();
        return mTimerModel.getTimerVibrate();
    }

    /**
     * @param enabled whether vibrate is enabled for all timers.
     */
    public void setTimerVibrate(boolean enabled) {
        Utils.enforceMainLooper();
        mTimerModel.setTimerVibrate(enabled);
    }



    //
    // Alarms
    //

    /**
     * @return the uri of the ringtone to which all new alarms default
     */
    public Uri getDefaultAlarmRingtoneUri() {
        Utils.enforceMainLooper();
        return mAlarmModel.getDefaultAlarmRingtoneUri();
    }

    /**
     * @param uri the uri of the ringtone to which future new alarms will default
     */
    public void setDefaultAlarmRingtoneUri(Uri uri) {
        Utils.enforceMainLooper();
        mAlarmModel.setDefaultAlarmRingtoneUri(uri);
    }

    /**
     * @param uri the uri of a ringtone
     * @return the title of the ringtone with the {@code uri}; {@code null} if it cannot be fetched
     */
    public String getAlarmRingtoneTitle(Uri uri) {
        Utils.enforceMainLooper();
        return mAlarmModel.getAlarmRingtoneTitle(uri);
    }


    /**
     * @return whether snooze by voice is enabled for all alarms.
     */
    public boolean getSnoozeByVoice() {
        Utils.enforceMainLooper();
        return mAlarmModel.getSnoozeByVoice();
    }

    /**
     * @param enabled whether snooze by voice is enabled for all alarms.
     */
    public void setSnoozeByVoice(boolean enabled) {
        Utils.enforceMainLooper();
        mAlarmModel.setSnoozeByVoice(enabled);
    }


    //
    // Stopwatch
    //

    /**
     * @param stopwatchListener to be notified when stopwatch changes or laps are added
     */
    public void addStopwatchListener(StopwatchListener stopwatchListener) {
        Utils.enforceMainLooper();
        mStopwatchModel.addStopwatchListener(stopwatchListener);
    }

    /**
     * @param stopwatchListener to no longer be notified when stopwatch changes or laps are added
     */
    public void removeStopwatchListener(StopwatchListener stopwatchListener) {
        Utils.enforceMainLooper();
        mStopwatchModel.removeStopwatchListener(stopwatchListener);
    }

    /**
     * @return the current state of the stopwatch
     */
    public Stopwatch getStopwatch() {
        Utils.enforceMainLooper();
        return mStopwatchModel.getStopwatch();
    }

    /**
     * @return the stopwatch after being started
     */
    public Stopwatch startStopwatch() {
        Utils.enforceMainLooper();
        return mStopwatchModel.setStopwatch(getStopwatch().start());
    }

    /**
     * @return the stopwatch after being paused
     */
    public Stopwatch pauseStopwatch() {
        Utils.enforceMainLooper();
        return mStopwatchModel.setStopwatch(getStopwatch().pause());
    }

    /**
     * @return the stopwatch after being reset
     */
    public Stopwatch resetStopwatch() {
        Utils.enforceMainLooper();
        return mStopwatchModel.setStopwatch(getStopwatch().reset());
    }

    /**
     * @return the laps recorded for this stopwatch
     */
    public List<Lap> getLaps() {
        Utils.enforceMainLooper();
        return mStopwatchModel.getLaps();
    }

    /**
     * @return a newly recorded lap completed now; {@code null} if no more laps can be added
     */
    public Lap addLap() {
        Utils.enforceMainLooper();
        return mStopwatchModel.addLap();
    }

    /**
     * @return {@code true} iff more laps can be recorded
     */
    public boolean canAddMoreLaps() {
        Utils.enforceMainLooper();
        return mStopwatchModel.canAddMoreLaps();
    }

    /**
     * @return the longest lap time of all recorded laps and the current lap
     */
    public long getLongestLapTime() {
        Utils.enforceMainLooper();
        return mStopwatchModel.getLongestLapTime();
    }

    /**
     * @param time a point in time after the end of the last lap
     * @return the elapsed time between the given {@code time} and the end of the previous lap
     */
    public long getCurrentLapTime(long time) {
        Utils.enforceMainLooper();
        return mStopwatchModel.getCurrentLapTime(time);
    }

    //
    // Widgets
    //

    /**
     * @param widgetClass indicates the type of widget being counted
     * @param count the number of widgets of the given type
     * @param eventCategoryId identifies the category of event to send
     */
    public void updateWidgetCount(Class widgetClass, int count, @StringRes int eventCategoryId) {
        Utils.enforceMainLooper();
        mWidgetModel.updateWidgetCount(widgetClass, count, eventCategoryId);
    }

    //
    // Settings
    //

    /**
     * @return the style of clock to display in the clock application
     */
    public ClockStyle getClockStyle() {
        Utils.enforceMainLooper();
        return mSettingsModel.getClockStyle();
    }

    /**
     * @return the style of clock to display in the clock screensaver
     */
    public ClockStyle getScreensaverClockStyle() {
        Utils.enforceMainLooper();
        return mSettingsModel.getScreensaverClockStyle();
    }

    /**
     * @return {@code true} if the screen saver should be dimmed for lower contrast at night
     */
    public boolean getScreensaverNightModeOn() {
        Utils.enforceMainLooper();
        return mSettingsModel.getScreensaverNightModeOn();
    }

    /**
     * @return {@code true} if the users wants to automatically show a clock for their home timezone
     *      when they have travelled outside of that timezone
     */
    public boolean getShowHomeClock() {
        Utils.enforceMainLooper();
        return mSettingsModel.getShowHomeClock();
    }

    /**
     * Used to execute a delegate runnable and track its completion.
     */
    private static class ExecutedRunnable implements Runnable {

        private final Runnable mDelegate;
        private boolean mExecuted;

        private ExecutedRunnable(Runnable delegate) {
            this.mDelegate = delegate;
        }

        @Override
        public void run() {
            mDelegate.run();

            synchronized (this) {
                mExecuted = true;
                notifyAll();
            }
        }

        private boolean isExecuted() {
            return mExecuted;
        }
    }
}

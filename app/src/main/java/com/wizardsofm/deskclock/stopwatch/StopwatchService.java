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

package com.wizardsofm.deskclock.stopwatch;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.wizardsofm.deskclock.HandleDeskClockApiCalls;
import com.wizardsofm.deskclock.data.DataModel;
import com.wizardsofm.deskclock.events.Events;

/**
 * This service exists solely to allow the stopwatch notification to alter the state of the
 * stopwatch without disturbing the notification shade. If an activity were used instead (even one
 * that is not displayed) the notification manager implicitly closes the notification shade which
 * clashes with the use case of starting/pausing/lapping/resetting the stopwatch without
 * disturbing the notification shade.
 */
public final class StopwatchService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case HandleDeskClockApiCalls.ACTION_START_STOPWATCH: {
                DataModel.getDataModel().startStopwatch();
                Events.sendStopwatchEvent(com.wizardsofm.deskclock.R.string.action_start, com.wizardsofm.deskclock.R.string.label_notification);
                break;
            }
            case HandleDeskClockApiCalls.ACTION_PAUSE_STOPWATCH: {
                DataModel.getDataModel().pauseStopwatch();
                Events.sendStopwatchEvent(com.wizardsofm.deskclock.R.string.action_pause, com.wizardsofm.deskclock.R.string.label_notification);
                break;
            }
            case HandleDeskClockApiCalls.ACTION_RESET_STOPWATCH: {
                DataModel.getDataModel().resetStopwatch();
                Events.sendStopwatchEvent(com.wizardsofm.deskclock.R.string.action_reset, com.wizardsofm.deskclock.R.string.label_notification);
                break;
            }
            case HandleDeskClockApiCalls.ACTION_LAP_STOPWATCH: {
                DataModel.getDataModel().addLap();
                Events.sendStopwatchEvent(com.wizardsofm.deskclock.R.string.action_lap, com.wizardsofm.deskclock.R.string.label_notification);
                break;
            }
        }

        return START_NOT_STICKY;
    }
}
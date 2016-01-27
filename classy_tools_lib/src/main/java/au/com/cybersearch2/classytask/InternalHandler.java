/**
    Copyright (C) 2014  www.cybersearch2.com.au

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/> */
// Original copyright - code mostly unchanged except for promotion to separate class
/*
 * Copyright (C) 2008 The Android Open Source Project, Romain Guy
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

package au.com.cybersearch2.classytask;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * InternalHandler
 * Message handler for posting messages from background thread to User thread
 * @author Romain Guy
 * 25/04/2014
 */
public class InternalHandler extends Handler
{
    public static final int MESSAGE_POST_RESULT = 0x1;
    public static final int MESSAGE_POST_PROGRESS = 0x2;
    public static final int MESSAGE_POST_CANCEL = 0x3;

    /**
     * Create default InternalHandler object
     */
    public InternalHandler()
    {
    }

    /**
     * Create InternalHandler object with callback
     * @param callback Callback
     */
    public InternalHandler(Callback callback)
    {
        super(callback);

    }

    /**
     * Create InternalHandler object with Looper
     * @param looper Looper
     */
    public InternalHandler(Looper looper)
    {
        super(looper);
    }

    /**
     * Create InternalHandler object with Looper and Callback
     * @param looper Looper
      * @param callback Callback
    */
    public InternalHandler(Looper looper, Callback callback)
    {
        super(looper, callback);

    }

    /**
     * Handle MESSAGE_POST_PROGRESS, MESSAGE_POST_RESULT or MESSAGE_POST_CANCEL
     * @see android.os.Handler#handleMessage(android.os.Message)
     */
    @Override
    public void handleMessage(Message msg) 
    {
            switch (msg.what) 
            {
                case MESSAGE_POST_PROGRESS:
                case MESSAGE_POST_RESULT:
                case MESSAGE_POST_CANCEL:
                {
                    ResultMessage resultMessage = (ResultMessage) msg.obj;
                    if (resultMessage != null)
                         resultMessage.getTask().run();
                    break;
                }
            }
    }
}

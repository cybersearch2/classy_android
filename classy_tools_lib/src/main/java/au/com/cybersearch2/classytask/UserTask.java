package au.com.cybersearch2.classytask;

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
// Original copyright - code mostly unchanged except for promotion of static classes to separate classes
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

/**
 * UserTask
 * @author Romain Guy
 * 25/04/2014
  * <p>UserTask enables proper and easy use of the UI thread. This class allows to
 * perform background operations and publish results on the UI thread without
 * having to manipulate threads and/or handlers.</p>
 *
 * <p>A user task is defined by a computation that runs on a background thread and
 * whose result is published on the UI thread. A user task is defined by 3 generic
 * types, called <code>Params</code>, <code>Progress</code> and <code>Result</code>,
 * and 4 steps, called <code>begin</code>, <code>doInBackground</code>,
 * <code>processProgress</code> and <code>end</code>.</p>
 *
 * <h2>Usage</h2>
 * <p>UserTask must be subclassed to be used. The subclass will override at least
 * one method ({@link #doInBackground()}), and most often will override a
 * second one ({@link #onPostExecute(Object)}.)</p>
 *
 * <p>Here is an example of subclassing:</p>
 * <pre>
 * private class DownloadFilesTask extends UserTask&lt;URL, Integer, Long&gt; {
 *     public File doInBackground(URL... urls) {
 *         int count = urls.length;
 *         long totalSize = 0;
 *         for (int i = 0; i < count; i++) {
 *             totalSize += Downloader.downloadFile(urls[i]);
 *             publishProgress((int) ((i / (float) count) * 100));
 *         }
 *     }
 *
 *     public void onProgressUpdate(Integer... progress) {
 *         setProgressPercent(progress[0]);
 *     }
 *
 *     public void onPostExecute(Long result) {
 *         showDialog("Downloaded " + result + " bytes");
 *     }
 * }
 * </pre>
 *
 * <p>Once created, a task is executed very simply:</p>
 * <pre>
 * new DownloadFilesTask().execute(new URL[] { ... });
 * </pre>
 *
 * <h2>User task's generic types</h2>
 * <p>The three types used by a user task are the following:</p>
 * <ol>
 *     <li><code>Params</code>, the type of the parameters sent to the task upon
 *     execution.</li>
 *     <li><code>Progress</code>, the type of the progress units published during
 *     the background computation.</li>
 *     <li><code>Result</code>, the type of the result of the background
 *     computation.</li>
 * </ol>
 * <p>Not all types are always used by a user task. To mark a type as unused,
 * simply use the type {@link Void}:</p>
 * <pre>
 * private class MyTask extends UserTask&lt;Void, Void, Void&gt; { ... }
 * </pre>
 *
 * <h2>The 4 steps</h2>
 * <p>When a user task is executed, the task goes through 4 steps:</p>
 * <ol>
 *     <li>{@link #onPreExecute()}, invoked on the UI thread immediately after the task
 *     is executed. This step is normally used to setup the task, for instance by
 *     showing a progress bar in the user interface.</li>
 *     <li>{@link #doInBackground(])}, invoked on the background thread
 *     immediately after {@link #onPreExecute ()} finishes executing. This step is used
 *     to perform background computation that can take a long time. The parameters
 *     of the user task are passed to this step. The result of the computation must
 *     be returned by this step and will be passed back to the last step. This step
 *     can also use {@link #publishProgress(Object[])} to publish one or more units
 *     of progress. These values are published on the UI thread, in the
 *     {@link #onProgressUpdate(Object[])} step.</li>
 *     <li>{@link #onProgressUpdate (Object[])}, invoked on the UI thread after a
 *     call to {@link #publishProgress(Object[])}. The timing of the execution is
 *     undefined. This method is used to display any form of progress in the user
 *     interface while the background computation is still executing. For instance,
 *     it can be used to animate a progress bar or show logs in a text field.</li>
 *     <li>{@link #onPostExecute (Object)}, invoked on the UI thread after the background
 *     computation finishes. The result of the background computation is passed to
 *     this step as a parameter.</li>
 * </ol>
 *
 * <h2>Threading rules</h2>
 * <p>There are a few threading rules that must be followed for this class to
 * work properly:</p>
 * <ul>
 *     <li>The task instance must be created on the UI thread.</li>
 *     <li>{@link #execute()} must be invoked on the UI thread.</li>
 *     <li>Do not call {@link #onPreExecute ()}, {@link #onPostExecute (Object)},
 *     {@link #doInBackground()}, {@link #onProgressUpdate (Object[])}
 *     manually.</li>
 *     <li>The task can be executed only once (an exception will be thrown if
 *     a second execution is attempted.)</li>
 * </ul>
*/
public abstract class UserTask<Progress> extends TaskRunner
{
    protected InternalHandler internalHandler;
    
    /**
     * Creates a new user task. This constructor must be invoked on the UI thread.
     */
    public UserTask(TaskManager taskManager, final InternalHandler internalHandler)
    {
         
        super(taskManager, new TaskMessenger(){

            /**
             * Post result to calling thread on background thread completion
             * @see au.com.cybersearch2.classytask.WorkerTask#sendResult(java.lang.Object)
             */
            @Override
            public void sendResult(final TaskRunner taskRunner, final Boolean result)
            {
                Runnable task = new Runnable(){
                    @Override
                    public void run() {
                        taskRunner.finish(result);
                    }};
                internalHandler.obtainMessage(
                        InternalHandler.MESSAGE_POST_RESULT, 
                        new ResultMessage(task))
                .sendToTarget();
            }

            /**
             * Post result to calling thread on background thread cancellation
             * @see au.com.cybersearch2.classytask.WorkerTask#sendCancel(java.lang.Object)
             */
            @Override
            public void sendCancel(final BackgroundTask backgroundTask, final Boolean result)
            {
                Runnable task = new Runnable(){
                    @Override
                    public void run() {
                        backgroundTask.onCancelled(result);
                    }};
                internalHandler.obtainMessage(
                        InternalHandler.MESSAGE_POST_CANCEL, 
                        new ResultMessage(task))
                .sendToTarget();
            }

        });
    }

     /**
      * This method can be invoked from {@link #doInBackground()} to
      * publish updates on the UI thread while the background computation is
      * still running. Each call to this method will trigger the execution of
      * {@link #onProgressUpdate(Object[])} on the UI thread.
      *
      * @param values The progress values to update the UI with.
      *
      * @see #onProgressUpdate (Object[])
      * @see #doInBackground()
      */
     protected final void publishProgress(final Progress... values) 
     {
         Runnable task = new Runnable(){
             @Override
             public void run() {
                 onProgressUpdate(values);
             }};
         internalHandler.obtainMessage(
                 InternalHandler.MESSAGE_POST_PROGRESS,
                 new ResultMessage(task))
         .sendToTarget();
     }

    /**
     * Runs on the UI thread after {@link #publishProgress(Object[])} is invoked.
     * The specified values are the values passed to {@link #publishProgress(Object[])}.
     *
     * @param values The values indicating progress.
     *
     * @see #publishProgress(Object[])
     * @see #doInBackground()
     */
    public void onProgressUpdate(Object[] values) 
    {
    }


}

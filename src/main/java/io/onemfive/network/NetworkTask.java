/*
  This is free and unencumbered software released into the public domain.

  Anyone is free to copy, modify, publish, use, compile, sell, or
  distribute this software, either in source code form or as a compiled
  binary, for any purpose, commercial or non-commercial, and by any
  means.

  In jurisdictions that recognize copyright laws, the author or authors
  of this software dedicate any and all copyright interest in the
  software to the public domain. We make this dedication for the benefit
  of the public at large and to the detriment of our heirs and
  successors. We intend this dedication to be an overt act of
  relinquishment in perpetuity of all present and future rights to this
  software under copyright law.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  OTHER DEALINGS IN THE SOFTWARE.

  For more information, please refer to <http://unlicense.org/>
 */
package io.onemfive.network;

import io.onemfive.util.tasks.BaseTask;
import io.onemfive.util.tasks.TaskRunner;

import java.util.Properties;

/**
 * A Task for the SensorsService.
 *
 * @author objectorange
 */
public abstract class NetworkTask extends BaseTask {

    protected Properties properties;
    protected long periodicity = 60 * 60 * 1000; // 1 hour as default
    protected long lastCompletionTime = 0L;
    protected boolean started = false;
    protected boolean completed = false;
    protected boolean longRunning = false;

    public NetworkTask(String taskName, TaskRunner taskRunner) {
        super(taskName, taskRunner);
        this.lastCompletionTime = System.currentTimeMillis();
    }

    public NetworkTask(String taskName, TaskRunner taskRunner, Properties properties) {
        super(taskName, taskRunner);
        this.properties = properties;
        this.lastCompletionTime = System.currentTimeMillis();
    }

    public NetworkTask(String taskName, TaskRunner taskRunner, Properties properties, long periodicity) {
       super(taskName, taskRunner);
        this.properties = properties;
        this.periodicity = periodicity;
        this.lastCompletionTime = System.currentTimeMillis();
    }

    public Boolean isLongRunning() {return longRunning;}

    public void setLastCompletionTime(long lastCompletionTime) {
        this.lastCompletionTime = lastCompletionTime;
    }

    public Long getLastCompletionTime() { return lastCompletionTime;}

    public Long getPeriodicity() {
        return periodicity;
    }
}

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
package io.onemfive.util.tasks;

import java.util.Map;

public interface Task extends Runnable {

    enum Status {Ready, Running, Completed}

    String getTaskName();
    void setParams(Map<Object, Object> params);
    void addParams(Map<Object, Object> params);
    Long getPeriodicity();
    void setPeriodicity(long periodicity);
    void setLastCompletionTime(Long lastCompletionTime);
    Long getLastCompletionTime();
    void setDelayed(Boolean delayed);
    Boolean getDelayed();
    void setDelayTimeMS(Long delayTimeMS);
    void setFixedDelay(Boolean fixedDelay);
    Boolean getFixedDelay();
    Long getDelayTimeMS();
    void setLongRunning(Boolean longRunning);
    Boolean getLongRunng();
    void setScheduled(Boolean scheduled);
    Boolean getScheduled();
    Boolean getSuccessful();
    Boolean execute();
    Boolean stop();
    Boolean forceStop();
    Status getStatus();
}

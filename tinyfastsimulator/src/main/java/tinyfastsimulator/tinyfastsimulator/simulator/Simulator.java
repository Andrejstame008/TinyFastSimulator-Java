/**
 * Copyright 2025 Alexander Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tinyfastsimulator.tinyfastsimulator.simulator;

import java.util.function.LongConsumer;

/**
 * Central simulator object
 */
public class Simulator {
  /**
   * Step size to increase the event list size
   */
  private final int EVENT_LIST_INCREMENT=10;

  /**
   * Times of the events waiting to be executed
   */
  private long[] eventTime;

  /**
   * Callback functions of the events waiting to be executed
   */
  private LongConsumer[] eventRun;

  /**
   * Number of used entries in the event list arrays
   */
  private int used;

  /**
   * Constructor
   */
  public Simulator() {
    eventTime=new long[EVENT_LIST_INCREMENT];
    eventRun=new LongConsumer[EVENT_LIST_INCREMENT];
    used=0;
  }

  /**
   * Adds an event.
   * @param time  Execution time
   * @param run Callback to be executed
   */
  public void add(final long time, final LongConsumer run) {
    /* Increase list size if needed */
    if (eventTime.length==used) {
      final long[] newEventTime=new long[eventTime.length+EVENT_LIST_INCREMENT];
      System.arraycopy(eventTime,0,newEventTime,0,eventTime.length);
      eventTime=newEventTime;
      final LongConsumer[] newEventRun=new LongConsumer[eventRun.length+EVENT_LIST_INCREMENT];
      System.arraycopy(eventRun,0,newEventRun,0,eventRun.length);
      eventRun=newEventRun;
    }

    /* List empty? */
    if (used==0) {
      eventTime[0]=time;
      eventRun[0]=run;
      used=1;
      return;
    }

    /* Find position in list */
    int index1=0;
    int index2=used-1;
    long time1=eventTime[index1];
    long time2=eventTime[index2];
    int index;
    if (time>time1) index=index1; else if (time<time2) index=index2+1; else {
      while (index2-index1>1) {
        final int indexMiddle=(index2-index1)/2;
        if (indexMiddle==index1) break;
        long timeMiddle=eventTime[indexMiddle];
        if (timeMiddle<time) index2=indexMiddle; else index1=indexMiddle;
      }
      index=index2+1;
      for (int i=index1;i<=index2;i++) if (time>eventTime[i]) {
        index=i;
        break;
      }
    }

    /* Insert into list */
    for (int i=used-1;i>=index;i--) {
      eventTime[i+1]=eventTime[i];
      eventRun[i+1]=eventRun[i];
    }
    eventTime[index]=time;
    eventRun[index]=run;
    used++;
  }

  /**
   * Executes all events.
   * @param showProgress  Show simulation progress
   * @return  Runtime in ms
   */
  public long run(final boolean showProgress) {
    final long start=System.currentTimeMillis();

    long count=0;
    while (used>0) {
      final long time=eventTime[used-1];
      final LongConsumer run=eventRun[used-1];
      used--;
      run.accept(time);

      if (showProgress && count%1000000==0) System.out.print(".");
      count++;
    }
    if (showProgress) System.out.println("");

    return System.currentTimeMillis()-start;
  }
}
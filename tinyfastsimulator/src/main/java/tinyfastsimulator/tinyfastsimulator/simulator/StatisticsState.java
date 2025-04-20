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

/**
 * Statistic class for recording states
 */
public class StatisticsState {
  /**
   * States multiplied by the times
   */
  private double sum;

  /**
   * Recorded time
   */
  private double time;

  /**
   * Current state
   */
  private int lastState;

  /**
   * Time at which the current state was reached
   */
  private double lastTime=-1;

  /*
   * Minimum recorded state
   */
  private int min;

  /**
   * Maximum recorded state
   */
  private int max;

  /**
   * Joins the data from two statistic objects.
   * @param stat1 Statistic object 1
   * @param stat2 Statistic object 2
   * @return  New statistic object containing the combined data
   */
  public static StatisticsState join(final StatisticsState stat1, final StatisticsState stat2) {
    final StatisticsState result=new StatisticsState();
    result.sum=stat1.sum+stat2.sum;
    result.time=stat1.time+stat2.time;
    result.min=Math.min(stat1.min,stat2.min);
    result.max=Math.max(stat1.max,stat2.max);
    return result;
  }

  /**
   * Records a state change.
   * @param time  Time of the state change
   * @param state New state
   */
  public void set(final double time, final int state) {
    if (lastTime>=0) {
      final double deltaTime=time-lastTime;
      if (deltaTime>0) {
        this.time+=deltaTime;
        sum+=deltaTime*lastState;
      }
      min=Math.min(min,lastState);
      max=Math.max(max,lastState);
    } else {
      min=state;
      max=state;
    }

    lastTime=time;
    lastState=state;
  }

  /**
   * Returns the average state.
   * @return  Average state
   */
  public double getMean() {
    if (time==0.0) return 0.0;
    return sum/time;
  }

  /**
   * Returns the minimum recorded state.
   * @return  Minimum recorded state
   */
  public int getMin() {
    return min;
  }

  /**
   * Returns the maximum recorded state.
   * @return  Maximum recorded state
   */
  public int getMax() {
    return max;
  }
}
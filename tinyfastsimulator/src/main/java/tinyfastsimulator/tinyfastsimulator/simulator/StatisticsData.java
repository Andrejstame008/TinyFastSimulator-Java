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

 import java.lang.Math;

/**
 * Statistic class for recording individual values
 */
public class StatisticsData {
  /**
   * Number of recorded values
   */
  private long count;

  /**
   * Sum of the recorded values
   */
  private double sum;

  /**
   * Sum of the squared recorded values
   */
  private double sum2;

  /**
   * Minimum of the recorded values
   */
  private double min;

  /**
   * Maximum of the recorded values
   */
  private double max;

  /**
   * Joins the data from two statistic objects.
   * @param stat1 Statistic object 1
   * @param stat2 Statistic object 2
   * @return  New statistic object containing the combined data
   */
  public static StatisticsData join(final StatisticsData stat1, final StatisticsData stat2) {
    final StatisticsData result=new StatisticsData();
    result.count=stat1.count+stat2.count;
    result.sum=stat1.sum+stat2.sum;
    result.sum2=stat1.sum2+stat2.sum2;
    result.min=Math.min(stat1.min,stat2.min);
    result.max=Math.max(stat1.max,stat2.max);
    return result;
  }

  /**
   * Records a single value.
   * @param value Value to be recorded
   */
  public void add(final double value) {
    count++;
    sum+=value;
    sum2+=value*value;
    if (count==1) {
      min=value;
      max=value;
    } else {
      min=Math.min(min,value);
      max=Math.max(max,value);
    }
  }

  /**
   * Returns the number of recorded values.
   * @return  Number of recorded values
   */
  public long getCount() {
    return count;
  }

  /**
   * Returns the mean of the recorded values.
   * @return Mean of the recorded values
   */
  public double getMean() {
    if (count==0) return 0.0;
    return sum/count;
  }

  /**
   * Returns the variance of the recorded values.
   * @return Variance of the recorded values
   */
  public double getVariance() {
    if (count==0) return 0.0;
    return sum2/(count-1)-(sum*sum)/count/(count-1);
  }

  /**
   * Returns the standard deviation of the recorded values.
   * @return Standard deviation of the recorded values
   */
  public double getSD() {
    return Math.sqrt(getVariance());
  }

  /**
   * Returns the coefficient of variation of the recorded values.
   * @return Coefficient of variation of the recorded values
   */
  public double getCV() {
    final double mean=getMean();
    if (mean==0.0) return 0.0;
    return getSD()/mean;
  }

  /**
   * Returns the minimum of the recorded values.
   * @return  Minimum of the recorded values
   */
  public double getMin() {
    return min;
  }

  /**
   * Returns the maximum of the recorded values.
   * @return  Maximum of the recorded values
   */
  public double getMax() {
    return max;
  }
}

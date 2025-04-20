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
package tinyfastsimulator.tinyfastsimulator;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.ToDoubleFunction;

import tinyfastsimulator.tinyfastsimulator.simulator.Simulator;
import tinyfastsimulator.tinyfastsimulator.simulator.StatisticsData;
import tinyfastsimulator.tinyfastsimulator.simulator.StatisticsState;
import tinyfastsimulator.tinyfastsimulator.simulator.Model;

/**
 * Model for G/G/c queue simulation
 */
public class QueueModel extends Model implements Cloneable {
  /**
   * Internal time base is 1/1000 second
   */
  private static final double SIM_TIME_FACTOR=1000;

  /**
   * Step size to increase the queue array length
   */
  private static final int QUEUE_INCREMENT=10;

  /* Static model data */

  /**
   * Callback for generating inter-arrival times
   */
  private final ToDoubleFunction<ThreadLocalRandom> interArrivalTime;

  /**
   * Callback for generating service times
   */
  private final ToDoubleFunction<ThreadLocalRandom> serviceTime;

  /**
   * Total number of operators
   */
  private final int cAvailable;

  /**
   * Number of arrivals to be simulated
   */
  public long arrivalGoal;

  /**
   * Show simulation logs?
   */
  private final boolean printLogs;

  /**
   * Load balancer (optional, can be null)
   */
  private final LongSupplier loadBalancer;

  /* Run time data */

  /**
   * Simulator systems
   */
  private final Simulator simulator=new Simulator();

  /**
   * Objects to generate pseudo random numbers on [0,1)
   */
  private ThreadLocalRandom random;

  /**
   * Number of busy operators
   */
  private int cInUse;

  /**
   * Number of simulated arrivals
   */
  private long arrivalCount;

  /**
   * Waiting time statistics
   */
  public StatisticsData waitingTimeStat=new StatisticsData();

  /**
   * Service time statistics
   */
  public StatisticsData serviceTimeStat=new StatisticsData();

  /**
   * Residence time statistics
   */
  public StatisticsData residenceTimeStat=new StatisticsData();

  /**
   * Queue length statistics
   */
  public StatisticsState queueLength=new StatisticsState();

  /**
   * Number of clients in the system statistics
   */
  public StatisticsState systemSize=new StatisticsState();

  /**
   * Number of busy operators statistics
   */
  public StatisticsState operatorsUsage=new StatisticsState();

  /**
   * Arrival times of the waiting clients
   */
  private long[] queue=new long[QUEUE_INCREMENT];

  /**
   * Number of used entries in the queue array
   */
  private int queueUsed;

  /**
   * Number of simulated events
   */
  public long eventCount;

  /**
   * Simulation runtime in MS
   */
  public long runtimeMS;

  /**
   * Callback for processing arrival events
   */
  private final LongConsumer arrival=t->runArrivalEvent(t);

  /**
   * Callback for processing service time end events
   */
  private final LongConsumer service=t->runOperatorAvailableEvent(t);

  /**
   * Constructor
   * @param interArrivalTime  Callback for generating inter-arrival times
   * @param serviceTime Callback for generating service times
   * @param c Number of operators in the system
   * @param arrivalGoal Number of arrivals to be simulated
   * @param printLogs Show simulation logs?
   */
  public QueueModel(final ToDoubleFunction<ThreadLocalRandom> interArrivalTime, final ToDoubleFunction<ThreadLocalRandom> serviceTime, final int c, final long arrivalGoal, final boolean printLogs, final LongSupplier loadBalancer) {
    this.interArrivalTime=interArrivalTime;
    this.serviceTime=serviceTime;
    cAvailable=c;
    this.arrivalGoal=arrivalGoal;
    this.printLogs=printLogs;
    this.loadBalancer=loadBalancer;
  }

  /**
   * Copy constructor
   * @param source  Source mode to be cloned
   */
  public QueueModel(final QueueModel source) {
    this(source.interArrivalTime,source.serviceTime,source.cAvailable,source.arrivalGoal,source.printLogs,source.loadBalancer);
  }

  /**
   * Clones the model.
   * @return Cloned model
   */
  public QueueModel clone() {
    return new QueueModel(this);
  }

  /**
   * Updates the statistics for the system states.
   * @param currentTime Current time
   */
  private void updateStateStatistics(final long currentTime) {
    final double time=currentTime/SIM_TIME_FACTOR;
    queueLength.set(time,queueUsed);
    systemSize.set(time,queueUsed+cInUse);
    operatorsUsage.set(time,cInUse);
  }

  /**
   * Records the time data for a single client in the client in the statistics.
   * @param waitingTime Waiting time for the client
   * @param serviceTime Service time for the client
   */
  private void recordCustomerStatistics(final double waitingTime, final double serviceTime) {
    waitingTimeStat.add(waitingTime);
    serviceTimeStat.add(serviceTime);
    residenceTimeStat.add(waitingTime+serviceTime);
  }

  /**
   * Generates and adds an arrival event
   * @param currentTime Current time (starting point for the inter-arrival time)
   */
  private void addArrivalEvent(final long currentTime) {
    if (arrivalCount>=arrivalGoal) {
      if (loadBalancer!=null) {
        final long moreArrivals=loadBalancer.getAsLong();
        if (moreArrivals>0) {
          arrivalGoal+=moreArrivals;
        } else {
          if (printLogs) System.out.println("  arrival goal reached; no more arrivals will be scheduled");
          return;
        }
      } else {
        if (printLogs) System.out.println("  arrival goal reached; no more arrivals will be scheduled");
        return;
      }
    }
    final long interArrivalTime=Math.round(this.interArrivalTime.applyAsDouble(random)*SIM_TIME_FACTOR);
    if (printLogs) System.out.println(String.format("  scheduled arrival at %f (=%f (now) + %f (inter-arrival))",(currentTime+interArrivalTime)/SIM_TIME_FACTOR,currentTime/SIM_TIME_FACTOR,interArrivalTime/SIM_TIME_FACTOR));
    simulator.add(currentTime+interArrivalTime,arrival);
    arrivalCount++;
  }

  /**
   * Starts a service process.
   * @param time  Time at which the service process starts
   */
  private void startServiceProcess(final long time) {
    /* Get waiting start time */
    final long startWaitng=queue[0];
    final double waitingTime=(time-startWaitng)/SIM_TIME_FACTOR;
    if (printLogs) System.out.println(String.format("  removed customer from queue; waiting time=%f",waitingTime));

    /* Remove customer from queue */
    for (int i=0;i<queueUsed-1;i++) queue[i]=queue[i+1];
    queueUsed--;

    /* Seize operator */
    cInUse++;
    updateStateStatistics(time);

    /* Get service time */
    final double serviceTime=this.serviceTime.applyAsDouble(random);
    final long endServiceTime=Math.round(serviceTime*SIM_TIME_FACTOR)+time;
    if (printLogs) {
      System.out.println(String.format("  service time=%f",serviceTime));
      System.out.println(String.format("  end of service process=%f",endServiceTime/SIM_TIME_FACTOR));
    }

    /* Record customer statistics */
    recordCustomerStatistics(waitingTime,serviceTime);

    /* Add service time end event */
    simulator.add(endServiceTime,service);
  }

  /**
   * Executes an arrival.
   * @param time  Current time
   */
  private void runArrivalEvent(final long time) {
    eventCount++;

    if (printLogs) System.out.println(String.format("%f: arrival",time/SIM_TIME_FACTOR));

    /* Add customer to queue */
    if (queueUsed==queue.length) {
      final long[] newQueue=new long[queue.length+QUEUE_INCREMENT];
      System.arraycopy(queue,0,newQueue,0,queue.length);
      queue=newQueue;
    }
    queue[queueUsed]=time;
    queueUsed++;

    if (cInUse<cAvailable) {
      /* Start service process */
      if (printLogs) System.out.println("  free operator is available -> start service");
      startServiceProcess(time);
    } else {
      /* Add customer to queue */
      if (printLogs) System.out.println("  no free operator available -> add to queue");
      updateStateStatistics(time);
    }

    /* Schedule next arrival */
    addArrivalEvent(time);
  }

  /**
   * Executes an operator gets free event.
   * @param time  Current time
   */
  private void runOperatorAvailableEvent(final long time) {
    eventCount++;

    if (printLogs) System.out.println(String.format("%f: service process done",time/SIM_TIME_FACTOR));

    /* Free operator */
    cInUse--;
    updateStateStatistics(time);

    /* Is a customer waiting? */
    if (queueUsed>0) {
      if (printLogs) System.out.println("  waiting customers available -> start service");
      startServiceProcess(time);
    }
  }

  /**
   * Runs the simulation.
   */
  public void run() {
    random=ThreadLocalRandom.current();
    addArrivalEvent(0);
    runtimeMS=simulator.run(printLogs);
  }

  /**
   * Joins the results from two simulations.
   * @param model1  Simulation 1 results
   * @param model2  Simulation 2 results
   * @return  New model with joined results
   */
  public static QueueModel join(final QueueModel model1, final QueueModel model2) {
    final QueueModel result=new QueueModel(model1);
    result.arrivalCount=model1.arrivalCount+model2.arrivalCount;
    result.arrivalGoal=model1.arrivalGoal+model2.arrivalGoal;
    result.waitingTimeStat=StatisticsData.join(model1.waitingTimeStat,model2.waitingTimeStat);
    result.serviceTimeStat=StatisticsData.join(model1.serviceTimeStat,model2.serviceTimeStat);
    result.residenceTimeStat=StatisticsData.join(model1.residenceTimeStat,model2.residenceTimeStat);
    result.queueLength=StatisticsState.join(model1.queueLength,model2.queueLength);
    result.systemSize=StatisticsState.join(model1.systemSize,model2.systemSize);
    result.operatorsUsage=StatisticsState.join(model1.operatorsUsage,model2.operatorsUsage);
    result.eventCount=model1.eventCount+model2.eventCount;
    result.runtimeMS=Math.max(model1.runtimeMS,model2.runtimeMS);
    return result;
  }
}
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

import java.util.Arrays;

import tinyfastsimulator.tinyfastsimulator.simulator.RandomNumbers;

/**
 * Main class
 */
public class Main {
  /**
   * Prints the simulation results.
   * @param *model  Simulation model
   * @param c Number of operators
   * @param threadCount  Number of used threads
   */
  private static void printResults(final QueueModel model, final int c, final int threadCount) {
    System.out.println(String.format("Processed events: %gM",model.eventCount/1000000.0));
    System.out.println(String.format("Simulated service processes: %gM",model.serviceTimeStat.getCount()/1000000.0));
    System.out.println("");
    System.out.println(String.format("Average waiting time: E[W]=%g",model.waitingTimeStat.getMean()));
    System.out.println(String.format("Standard deviation of the waiting times: Std[W]=%g",model.waitingTimeStat.getSD()));
    System.out.println(String.format("Coefficient of variation of the waiting times CV[W]=%g",model.waitingTimeStat.getCV()));
    System.out.println(String.format("Minimum waiting time: Min[W]=%g",model.waitingTimeStat.getMin()));
    System.out.println(String.format("Maximum waiting time: Max[W]=%g",model.waitingTimeStat.getMax()));
    System.out.println("");
    System.out.println(String.format("Average service time: E[S]=%g",model.serviceTimeStat.getMean()));
    System.out.println(String.format("Standard deviation of the service times: Std[S]=%g",model.serviceTimeStat.getSD()));
    System.out.println(String.format("Coefficient of variation of the service times CV[S]=%g",model.serviceTimeStat.getCV()));
    System.out.println(String.format("Minimum service time: Min[S]=%g",model.serviceTimeStat.getMin()));
    System.out.println(String.format("Maximum service time: Max[S]=%g",model.serviceTimeStat.getMax()   ));
    System.out.println("");
    System.out.println(String.format("Average residence time: E[V]=%g",model.residenceTimeStat.getMean()));
    System.out.println(String.format("Standard deviation of the residence times: Std[V]=%g",model.residenceTimeStat.getSD()));
    System.out.println(String.format("Coefficient of residence of the service times CV[v]=%g",model.residenceTimeStat.getCV()));
    System.out.println(String.format("Minimum residence time: Min[V]=%g",model.residenceTimeStat.getMin()));
    System.out.println(String.format("Maximum residence time: Max[V]=%g",model.residenceTimeStat.getMax()));
    System.out.println("");
    System.out.println(String.format("Average queue length: E[NQ]=%g",model.queueLength.getMean()));
    System.out.println(String.format("Minimum queue length: Min[NQ]=%d",model.queueLength.getMin()));
    System.out.println(String.format("Maximum queue length: Max[NQ]=%d",model.queueLength.getMax()));
    System.out.println("");
    System.out.println(String.format("Average number of customers in system: E[N]=%g",model.systemSize.getMean()));
    System.out.println(String.format("Minimum number of customers in system: Min[N]=%d",model.systemSize.getMin()));
    System.out.println(String.format("Maximum number of customers in system: Max[N]=%d",model.systemSize.getMax()));
    System.out.println("");
    System.out.println(String.format("Average number of busy operators: %g",model.operatorsUsage.getMean()));
    System.out.println(String.format("Minimum number of busy operators: %d",model.operatorsUsage.getMin()));
    System.out.println(String.format("Maximum number of busy operators: %d",model.operatorsUsage.getMax()));
    System.out.println(String.format("Work load rho=%g%%",model.operatorsUsage.getMean()/c*100));
    System.out.println("");
    System.out.println(String.format("Wall clock time: %dms",model.runtimeMS));
    if (threadCount>1) System.out.println(String.format("Number of parallel threads: %d",threadCount));
    if (model.runtimeMS>0) System.out.println(String.format("Events/second: %gM",model.eventCount*1000/model.runtimeMS/1000000.0));
    if (threadCount>1 && model.runtimeMS>0) System.out.println(String.format("Events/second/thread: %gM",model.eventCount*1000/model.runtimeMS/1000000.0/threadCount));
  }

  /**
   * Main functions
   * @param args    Command-line parameters
   */
  public static void main(final String[] args) {
    /* Load parameters */
    final var parameters=new Parameters(args);

    /* Start */
    String arrivalMode;
    if (parameters.cvI==1.0) {
      arrivalMode=String.format("EI[I]=%g (exp)",parameters.EI);
    } else {
        arrivalMode=String.format("EI[I]=%g, CV[I]=%g (log-normal)",parameters.EI,parameters.cvI);
    }
    String serviceMode;
    if (parameters.cvI==1.0) {
      serviceMode=String.format("EI[S]=%g (exp)",parameters.ES);
    } else {
      serviceMode=String.format("EI[S]=%g, CV[S]=%g (log-normal)",parameters.ES,parameters.cvS);
    }
    System.out.println("Simple discrete event-oriented simulator for a G/G/c model");
    System.out.println(String.format("with %s, %s, c=%d, arrivals=%gM, threads=%d\n",arrivalMode,serviceMode,parameters.c,parameters.arrivalCount/1000000.0,parameters.threadCount));

    /* Initialize models */
    final QueueModel[] queueModels=new QueueModel[parameters.threadCount];
    final var transformEI=RandomNumbers.getRandomNumbersLambda((parameters.cvI==1.0)?RandomNumbers.RandomDistribution.EXP:RandomNumbers.RandomDistribution.LOG_NORMAL,parameters.EI,parameters.cvI*parameters.EI);
    final var transformES=RandomNumbers.getRandomNumbersLambda((parameters.cvS==1.0)?RandomNumbers.RandomDistribution.EXP:RandomNumbers.RandomDistribution.LOG_NORMAL,parameters.ES,parameters.cvS*parameters.ES);
    if (parameters.loadBalancer) {
      /* Use load balancer */
      final var loadBalancer=new LoadBalancer(parameters.arrivalCount,parameters.threadCount);
      for (int i=0;i<parameters.threadCount;i++) {
        queueModels[i]=new QueueModel(transformEI,transformES,parameters.c,0,false,()->loadBalancer.getNextTask());
      }
    } else {
      /* Just split arrivals into equal sized parts */
      long sum=0;
      for (int i=0;i<parameters.threadCount-1;i++) {
        final long value=parameters.arrivalCount/parameters.threadCount;
        sum+=value;
        queueModels[i]=new QueueModel(transformEI,transformES,parameters.c,value,false,null);
      }
      queueModels[queueModels.length-1]=new QueueModel(transformEI,transformES,parameters.c,parameters.arrivalCount-sum,false,null);
    }

    /* Run simulation threads */
    final Thread[] threads=new Thread[parameters.threadCount];
    for (int i=0;i<threads.length;i++) {
        queueModels[i]=new QueueModel(queueModels[i]); /* Make memory allocations from within the thread. - Very important on NUMA systems.  */
        final QueueModel model=queueModels[i];
        threads[i]=new Thread(()->model.run());
        threads[i].start();
    }
    try {
      for (int i=0;i<threads.length;i++) threads[i].join();
    } catch (InterruptedException e) {}

    /* Print results */
    QueueModel joinedModel=queueModels[0];
    for (int i=1;i<queueModels.length;i++) joinedModel=QueueModel.join(joinedModel,queueModels[i]);
    printResults(joinedModel,parameters.c,parameters.threadCount);

    if (parameters.showTimes) {
      System.out.println("");
      System.out.println("Runtimes per Thread:");
      Arrays.stream(queueModels).map(model->model.runtimeMS).forEach(System.out::println);
    }

    if (parameters.loadBalancer) {
      System.out.println("");
      System.out.println("Arrivals per Thread:");
      Arrays.stream(queueModels).map(model->model.arrivalGoal).forEach(System.out::println);
    }

    /* Quit */
    System.out.println("");
  }
}
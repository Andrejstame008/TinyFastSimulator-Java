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

 import java.util.function.DoubleConsumer;
 import java.util.function.IntConsumer;
 import java.util.function.LongConsumer;

public class Parameters {
  /**
   * Default mean inter-arrival time E[I]
   */
  private static final double default_EI=100;

  /**
   * Default coefficient of variation of the inter-arrival times CV[I]
   */
  private static final double default_cvI=1;

  /**
   * Default mean service time E[S]
   */
  private static final double default_ES=80;

  /**
   * Default coefficient of variation of the service times CV[S]
   */
  private static final double default_cvS=1;

  /**
   * Default number of operators
   */
  private static final int default_c=1;

  /**
   * Default arrival count
   */
  private static final long default_arrival_count=100000000L;

  /**
   * Mean inter-arrival time E[I]
   */
  public double EI;

  /**
   * Coefficient of variation of the inter-arrival times CV[I]
   */
  public double cvI;

  /**
   * Mean service time E[S]
   */
  public double ES;

  /**
   * Coefficient of variation of the service times CV[S]
   */
  public double cvS;

  /**
   * Number of operators
   */
  public int c;

  /**
   * Arrival count
   */
  public long arrivalCount;

  /**
   * Thread count
   */
  public int threadCount;

  /**
   * Show runtimes of the individual threads
   */
  public boolean showTimes;

  /**
   * Use load balancer
   */
  public boolean loadBalancer;

  private static void loadDouble(final String parameter, final String label, DoubleConsumer lambda, final boolean allowZero) {
    if (parameter.length()==label.length()) return;
    try {
      final Double D=Double.valueOf(parameter.substring(label.length()));
      if (D!=null) {
        if (!allowZero && D==0.0) return;
        lambda.accept(D);
      }
    } catch (NumberFormatException e) {
      return;
    }
  }

  private static void loadInt(final String parameter, final String label, IntConsumer lambda, final boolean allowZero) {
    if (parameter.length()==label.length()) return;
    try {
      final Integer I=Integer.valueOf(parameter.substring(label.length()));
      if (I!=null) {
        if (!allowZero && I==0) return;
        lambda.accept(I);
      }
    } catch (NumberFormatException e) {
      return;
    }
  }

  private static void loadLong(final String parameter, final String label, LongConsumer lambda, final boolean allowZero) {
    if (parameter.length()==label.length()) return;
    try {
      final Long L=Long.valueOf(parameter.substring(label.length()));
      if (L!=null) {
        if (!allowZero && L==0) return;
        lambda.accept(L);
      }
    } catch (NumberFormatException e) {
      return;
    }
  }

  public Parameters(final String args[]) {
    EI=default_EI;
    cvI=default_cvI;
    ES=default_ES;
    cvS=default_cvS;
    c=default_c;
    arrivalCount=default_arrival_count;
    threadCount=Runtime.getRuntime().availableProcessors();
    showTimes=false;

    boolean multiply_arrivals_by_threads=false;

    for (var arg: args) {
      final String parameter=arg.toLowerCase();
      if (parameter.startsWith("ei=")) loadDouble(parameter,"ei=",d->{EI=d;},false);
      if (parameter.startsWith("cvi=")) loadDouble(parameter,"cvi=",d->{cvI=d;},true);
      if (parameter.startsWith("es=")) loadDouble(parameter,"es=",d->{ES=d;},false);
      if (parameter.startsWith("cvs=")) loadDouble(parameter,"cvs=",d->{cvS=d;},true);
      if (parameter.startsWith("c=")) loadInt(parameter,"c=",i->{c=i;},false);
      if (parameter.startsWith("arrivals=")) loadLong(parameter,"arrivals=",l->{arrivalCount=l;},false);
      if (parameter.startsWith("threads=")) loadInt(parameter,"threads=",i->{threadCount=i;},false);
      if (parameter.startsWith("increase_arrivals")) multiply_arrivals_by_threads=true;
      if (parameter.startsWith("show_times")) showTimes=true;
      if (parameter.startsWith("load_balancer")) loadBalancer=true;
    }
    if (multiply_arrivals_by_threads) arrivalCount*=threadCount;
  }
}
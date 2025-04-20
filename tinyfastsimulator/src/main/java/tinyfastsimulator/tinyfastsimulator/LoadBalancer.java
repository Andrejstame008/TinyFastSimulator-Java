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

/**
 * Optional load balancer system to harmonize the simulation thread runtimes
 */
public class LoadBalancer {
    /**
     * Total number of arrivals to be simulated
     */
    public final long totalArrivals;

    /**
     * Number of threads
     */
    public final int threadCount;

    /**
     * Number of arrivals waiting to be assigned to the threads
     */
    private long arrivalsToBeSimulated;

    /**
     * Arrival assignment per request
     */
    private final long arrivalsPerRequest;

    /**
     * Constructor
     * @param totalArrivals Total number of arrivals to be simulated
     * @param threadCount   Number of threads
     */
    public LoadBalancer(final long totalArrivals, final int threadCount) {
        this.totalArrivals=totalArrivals;
        this.threadCount=threadCount;
        arrivalsToBeSimulated=totalArrivals;
        arrivalsPerRequest=(long)Math.ceil(totalArrivals/100.0/threadCount);
    }

    /**
     * Request a next package of arrivals to be simulated by a thread.
     * @return  Number of arrivals or 0, if no more arrivals are waiting
     */
    public synchronized long getNextTask() {
        final long result=Math.min(arrivalsToBeSimulated,arrivalsPerRequest);
        arrivalsToBeSimulated-=result;
        return result;
    }
}

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
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToDoubleFunction;

/**
 * Static functions for generating pseudo random numbers
 * for different distributions.
 */
public class RandomNumbers {

  /**
   * Private constructor - This class cannot be instanced.
   */
  private RandomNumbers() {
  }

  /**
   * Generate an exponentially distributed pseudo random number.
   * @param random  Pseudo random numbers generator (for generating numbers on [0,1))
   * @param mean  Mean
   * @return  Pseudo random number
   */
  public static double exp(final ThreadLocalRandom random, final double mean) {
    final double lambda=1/mean;
    final double u=random.nextDouble();
    /* u=F(x)=1-exp(-lambda*x) <=> x=-log(1-u)/lambda */
    return -Math.log(1-u)/lambda;
  }

  /**
   * Generate a normal distributed pseudo random number.
   * @param random  Pseudo random numbers generator (for generating numbers on [0,1))
   * @param mean  Mean
   * @param sd  Standard deviation
   * @return  Pseudo random number
   */
  public static double normal(final ThreadLocalRandom random, final double mean, final double sd) {
  double q=10, u=0, v=0;
  while (q==0 || q>=1) {
    u=2*random.nextDouble()-1;
    v=2*random.nextDouble()-1;
    q=Math.pow(u,2)+Math.pow(v,2);
	}
  final double p=Math.sqrt(-2*Math.log(q)/q);
  return mean+sd*u*p;
}

  /**
   * Generate a log-normal distributed pseudo random number.
   * @param random  Pseudo random numbers generator (for generating numbers on [0,1))
   * @param mean  Mean
   * @param sd  Standard deviation
   * @return  Pseudo random number
   */
public static double logNormal(final ThreadLocalRandom random, final double mean, final double sd) {
  final double sigma2=Math.log(Math.pow(sd/mean,2)+1);
	final double mu=Math.log(mean)-sigma2/2;
  final double sigma=Math.sqrt(sigma2);

  double q=10, u=0, v=0;
  while (q==0 || q>=1) {
    u=2*random.nextDouble()-1;
    v=2*random.nextDouble()-1;
    q=Math.pow(u,2)+Math.pow(v,2);
	}
	final double p=Math.sqrt(-2*Math.log(q)/q);
	return Math.exp(mu+sigma*u*p);
  }

  /**
   * Type of distribution
   * @see #getRandomNumbersLambda(RandomDistribution, double, double)
   */
  public static enum RandomDistribution {
    /** Exponentially distribution */
    EXP,
    /** Normal distribution */
    NORMAL,
    /** Log-normal distribution */
    LOG_NORMAL
  }

  /**
   * Generates a lambda expression for generating pseudo random numbers.
   * @param distribution  Type of distribution
   * @param mean  Mean
   * @param sd  Standard deviation
   * @return  Lambda expression for generating pseudo random numbers
   */
  public static ToDoubleFunction<ThreadLocalRandom> getRandomNumbersLambda(final RandomDistribution distribution, final double mean, final double sd) {
    switch (distribution) {
      case EXP: return random->exp(random,mean);
      case NORMAL: return random->normal(random,mean,sd);
      case LOG_NORMAL: return random->logNormal(random,mean,sd);
    }
    return null;
  }
}
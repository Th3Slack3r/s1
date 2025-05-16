package com.google.common.util.concurrent;

import java.util.concurrent.TimeUnit;

abstract class SmoothRateLimiter extends RateLimiter {
  double storedPermits;
  
  double maxPermits;
  
  double stableIntervalMicros;
  
  static final class SmoothWarmingUp extends SmoothRateLimiter {
    private final long warmupPeriodMicros;
    
    private double slope;
    
    private double halfPermits;
    
    SmoothWarmingUp(RateLimiter.SleepingStopwatch stopwatch, long warmupPeriod, TimeUnit timeUnit) {
      super(stopwatch);
      this.warmupPeriodMicros = timeUnit.toMicros(warmupPeriod);
    }
    
    void doSetRate(double permitsPerSecond, double stableIntervalMicros) {
      double oldMaxPermits = this.maxPermits;
      this.maxPermits = this.warmupPeriodMicros / stableIntervalMicros;
      this.halfPermits = this.maxPermits / 2.0D;
      double coldIntervalMicros = stableIntervalMicros * 3.0D;
      this.slope = (coldIntervalMicros - stableIntervalMicros) / this.halfPermits;
      if (oldMaxPermits == Double.POSITIVE_INFINITY) {
        this.storedPermits = 0.0D;
      } else {
        this.storedPermits = (oldMaxPermits == 0.0D) ? this.maxPermits : (this.storedPermits * this.maxPermits / oldMaxPermits);
      } 
    }
    
    long storedPermitsToWaitTime(double storedPermits, double permitsToTake) {
      double availablePermitsAboveHalf = storedPermits - this.halfPermits;
      long micros = 0L;
      if (availablePermitsAboveHalf > 0.0D) {
        double permitsAboveHalfToTake = Math.min(availablePermitsAboveHalf, permitsToTake);
        micros = (long)(permitsAboveHalfToTake * (permitsToTime(availablePermitsAboveHalf) + permitsToTime(availablePermitsAboveHalf - permitsAboveHalfToTake)) / 2.0D);
        permitsToTake -= permitsAboveHalfToTake;
      } 
      micros = (long)(micros + this.stableIntervalMicros * permitsToTake);
      return micros;
    }
    
    private double permitsToTime(double permits) {
      return this.stableIntervalMicros + permits * this.slope;
    }
  }
  
  static final class SmoothBursty extends SmoothRateLimiter {
    final double maxBurstSeconds;
    
    SmoothBursty(RateLimiter.SleepingStopwatch stopwatch, double maxBurstSeconds) {
      super(stopwatch);
      this.maxBurstSeconds = maxBurstSeconds;
    }
    
    void doSetRate(double permitsPerSecond, double stableIntervalMicros) {
      double oldMaxPermits = this.maxPermits;
      this.maxPermits = this.maxBurstSeconds * permitsPerSecond;
      if (oldMaxPermits == Double.POSITIVE_INFINITY) {
        this.storedPermits = this.maxPermits;
      } else {
        this.storedPermits = (oldMaxPermits == 0.0D) ? 0.0D : (this.storedPermits * this.maxPermits / oldMaxPermits);
      } 
    }
    
    long storedPermitsToWaitTime(double storedPermits, double permitsToTake) {
      return 0L;
    }
  }
  
  private long nextFreeTicketMicros = 0L;
  
  private SmoothRateLimiter(RateLimiter.SleepingStopwatch stopwatch) {
    super(stopwatch);
  }
  
  final void doSetRate(double permitsPerSecond, long nowMicros) {
    resync(nowMicros);
    double stableIntervalMicros = TimeUnit.SECONDS.toMicros(1L) / permitsPerSecond;
    this.stableIntervalMicros = stableIntervalMicros;
    doSetRate(permitsPerSecond, stableIntervalMicros);
  }
  
  final double doGetRate() {
    return TimeUnit.SECONDS.toMicros(1L) / this.stableIntervalMicros;
  }
  
  final long queryEarliestAvailable(long nowMicros) {
    return this.nextFreeTicketMicros;
  }
  
  final long reserveEarliestAvailable(int requiredPermits, long nowMicros) {
    resync(nowMicros);
    long returnValue = this.nextFreeTicketMicros;
    double storedPermitsToSpend = Math.min(requiredPermits, this.storedPermits);
    double freshPermits = requiredPermits - storedPermitsToSpend;
    long waitMicros = storedPermitsToWaitTime(this.storedPermits, storedPermitsToSpend) + (long)(freshPermits * this.stableIntervalMicros);
    this.nextFreeTicketMicros += waitMicros;
    this.storedPermits -= storedPermitsToSpend;
    return returnValue;
  }
  
  private void resync(long nowMicros) {
    if (nowMicros > this.nextFreeTicketMicros) {
      this.storedPermits = Math.min(this.maxPermits, this.storedPermits + (nowMicros - this.nextFreeTicketMicros) / this.stableIntervalMicros);
      this.nextFreeTicketMicros = nowMicros;
    } 
  }
  
  abstract void doSetRate(double paramDouble1, double paramDouble2);
  
  abstract long storedPermitsToWaitTime(double paramDouble1, double paramDouble2);
}

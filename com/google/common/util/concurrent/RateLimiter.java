package com.google.common.util.concurrent;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@Beta
public abstract class RateLimiter {
  private final SleepingStopwatch stopwatch;
  
  private volatile Object mutexDoNotUseDirectly;
  
  public static RateLimiter create(double permitsPerSecond) {
    return create(SleepingStopwatch.createFromSystemTimer(), permitsPerSecond);
  }
  
  @VisibleForTesting
  static RateLimiter create(SleepingStopwatch stopwatch, double permitsPerSecond) {
    RateLimiter rateLimiter = new SmoothRateLimiter.SmoothBursty(stopwatch, 1.0D);
    rateLimiter.setRate(permitsPerSecond);
    return rateLimiter;
  }
  
  public static RateLimiter create(double permitsPerSecond, long warmupPeriod, TimeUnit unit) {
    Preconditions.checkArgument((warmupPeriod >= 0L), "warmupPeriod must not be negative: %s", new Object[] { Long.valueOf(warmupPeriod) });
    return create(SleepingStopwatch.createFromSystemTimer(), permitsPerSecond, warmupPeriod, unit);
  }
  
  @VisibleForTesting
  static RateLimiter create(SleepingStopwatch stopwatch, double permitsPerSecond, long warmupPeriod, TimeUnit unit) {
    RateLimiter rateLimiter = new SmoothRateLimiter.SmoothWarmingUp(stopwatch, warmupPeriod, unit);
    rateLimiter.setRate(permitsPerSecond);
    return rateLimiter;
  }
  
  private Object mutex() {
    Object mutex = this.mutexDoNotUseDirectly;
    if (mutex == null)
      synchronized (this) {
        mutex = this.mutexDoNotUseDirectly;
        if (mutex == null)
          this.mutexDoNotUseDirectly = mutex = new Object(); 
      }  
    return mutex;
  }
  
  RateLimiter(SleepingStopwatch stopwatch) {
    this.stopwatch = (SleepingStopwatch)Preconditions.checkNotNull(stopwatch);
  }
  
  public final void setRate(double permitsPerSecond) {
    Preconditions.checkArgument((permitsPerSecond > 0.0D && !Double.isNaN(permitsPerSecond)), "rate must be positive");
    synchronized (mutex()) {
      doSetRate(permitsPerSecond, this.stopwatch.readMicros());
    } 
  }
  
  abstract void doSetRate(double paramDouble, long paramLong);
  
  public final double getRate() {
    synchronized (mutex()) {
      return doGetRate();
    } 
  }
  
  abstract double doGetRate();
  
  public double acquire() {
    return acquire(1);
  }
  
  public double acquire(int permits) {
    long microsToWait = reserve(permits);
    this.stopwatch.sleepMicrosUninterruptibly(microsToWait);
    return 1.0D * microsToWait / TimeUnit.SECONDS.toMicros(1L);
  }
  
  final long reserve(int permits) {
    checkPermits(permits);
    synchronized (mutex()) {
      return reserveAndGetWaitLength(permits, this.stopwatch.readMicros());
    } 
  }
  
  public boolean tryAcquire(long timeout, TimeUnit unit) {
    return tryAcquire(1, timeout, unit);
  }
  
  public boolean tryAcquire(int permits) {
    return tryAcquire(permits, 0L, TimeUnit.MICROSECONDS);
  }
  
  public boolean tryAcquire() {
    return tryAcquire(1, 0L, TimeUnit.MICROSECONDS);
  }
  
  public boolean tryAcquire(int permits, long timeout, TimeUnit unit) {
    long microsToWait, timeoutMicros = Math.max(unit.toMicros(timeout), 0L);
    checkPermits(permits);
    synchronized (mutex()) {
      long nowMicros = this.stopwatch.readMicros();
      if (!canAcquire(nowMicros, timeoutMicros))
        return false; 
      microsToWait = reserveAndGetWaitLength(permits, nowMicros);
    } 
    this.stopwatch.sleepMicrosUninterruptibly(microsToWait);
    return true;
  }
  
  private boolean canAcquire(long nowMicros, long timeoutMicros) {
    return (queryEarliestAvailable(nowMicros) - timeoutMicros <= nowMicros);
  }
  
  final long reserveAndGetWaitLength(int permits, long nowMicros) {
    long momentAvailable = reserveEarliestAvailable(permits, nowMicros);
    return Math.max(momentAvailable - nowMicros, 0L);
  }
  
  abstract long queryEarliestAvailable(long paramLong);
  
  abstract long reserveEarliestAvailable(int paramInt, long paramLong);
  
  public String toString() {
    return String.format("RateLimiter[stableRate=%3.1fqps]", new Object[] { Double.valueOf(getRate()) });
  }
  
  @VisibleForTesting
  static abstract class SleepingStopwatch {
    abstract long readMicros();
    
    abstract void sleepMicrosUninterruptibly(long param1Long);
    
    static final SleepingStopwatch createFromSystemTimer() {
      return new SleepingStopwatch() {
          final Stopwatch stopwatch = Stopwatch.createStarted();
          
          long readMicros() {
            return this.stopwatch.elapsed(TimeUnit.MICROSECONDS);
          }
          
          void sleepMicrosUninterruptibly(long micros) {
            if (micros > 0L)
              Uninterruptibles.sleepUninterruptibly(micros, TimeUnit.MICROSECONDS); 
          }
        };
    }
  }
  
  private static int checkPermits(int permits) {
    Preconditions.checkArgument((permits > 0), "Requested permits (%s) must be positive", new Object[] { Integer.valueOf(permits) });
    return permits;
  }
}

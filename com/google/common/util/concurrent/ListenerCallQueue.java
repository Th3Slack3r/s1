package com.google.common.util.concurrent;

import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

final class ListenerCallQueue<L> implements Runnable {
  private static final Logger logger = Logger.getLogger(ListenerCallQueue.class.getName());
  
  private final L listener;
  
  private final Executor executor;
  
  static abstract class Callback<L> {
    private final String methodCall;
    
    Callback(String methodCall) {
      this.methodCall = methodCall;
    }
    
    void enqueueOn(Iterable<ListenerCallQueue<L>> queues) {
      for (ListenerCallQueue<L> queue : queues)
        queue.add(this); 
    }
    
    abstract void call(L param1L);
  }
  
  @GuardedBy("this")
  private final Queue<Callback<L>> waitQueue = Queues.newArrayDeque();
  
  @GuardedBy("this")
  private boolean isThreadScheduled;
  
  ListenerCallQueue(L listener, Executor executor) {
    this.listener = (L)Preconditions.checkNotNull(listener);
    this.executor = (Executor)Preconditions.checkNotNull(executor);
  }
  
  synchronized void add(Callback<L> callback) {
    this.waitQueue.add(callback);
  }
  
  void execute() {
    boolean scheduleTaskRunner = false;
    synchronized (this) {
      if (!this.isThreadScheduled) {
        this.isThreadScheduled = true;
        scheduleTaskRunner = true;
      } 
    } 
    if (scheduleTaskRunner)
      try {
        this.executor.execute(this);
      } catch (RuntimeException e) {
        synchronized (this) {
          this.isThreadScheduled = false;
        } 
        String str1 = String.valueOf(String.valueOf(this.listener)), str2 = String.valueOf(String.valueOf(this.executor));
        logger.log(Level.SEVERE, (new StringBuilder(42 + str1.length() + str2.length())).append("Exception while running callbacks for ").append(str1).append(" on ").append(str2).toString(), e);
        throw e;
      }  
  }
  
  public void run() {
    boolean stillRunning = true;
    try {
      while (true) {
        Callback<L> nextToRun;
        synchronized (this) {
          Preconditions.checkState(this.isThreadScheduled);
          nextToRun = this.waitQueue.poll();
          if (nextToRun == null) {
            this.isThreadScheduled = false;
            stillRunning = false;
            break;
          } 
        } 
        try {
          nextToRun.call(this.listener);
        } catch (RuntimeException e) {
          String str1 = String.valueOf(String.valueOf(this.listener)), str2 = String.valueOf(String.valueOf(nextToRun.methodCall));
          logger.log(Level.SEVERE, (new StringBuilder(37 + str1.length() + str2.length())).append("Exception while executing callback: ").append(str1).append(".").append(str2).toString(), e);
        } 
      } 
    } finally {
      if (stillRunning)
        synchronized (this) {
          this.isThreadScheduled = false;
        }  
    } 
  }
}

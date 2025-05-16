package com.google.common.util.concurrent;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.Immutable;

@Beta
public abstract class AbstractService implements Service {
  private static final ListenerCallQueue.Callback<Service.Listener> STARTING_CALLBACK = new ListenerCallQueue.Callback<Service.Listener>("starting()") {
      void call(Service.Listener listener) {
        listener.starting();
      }
    };
  
  private static final ListenerCallQueue.Callback<Service.Listener> RUNNING_CALLBACK = new ListenerCallQueue.Callback<Service.Listener>("running()") {
      void call(Service.Listener listener) {
        listener.running();
      }
    };
  
  private static final ListenerCallQueue.Callback<Service.Listener> STOPPING_FROM_STARTING_CALLBACK = stoppingCallback(Service.State.STARTING);
  
  private static final ListenerCallQueue.Callback<Service.Listener> STOPPING_FROM_RUNNING_CALLBACK = stoppingCallback(Service.State.RUNNING);
  
  private static final ListenerCallQueue.Callback<Service.Listener> TERMINATED_FROM_NEW_CALLBACK = terminatedCallback(Service.State.NEW);
  
  private static final ListenerCallQueue.Callback<Service.Listener> TERMINATED_FROM_RUNNING_CALLBACK = terminatedCallback(Service.State.RUNNING);
  
  private static final ListenerCallQueue.Callback<Service.Listener> TERMINATED_FROM_STOPPING_CALLBACK = terminatedCallback(Service.State.STOPPING);
  
  private static ListenerCallQueue.Callback<Service.Listener> terminatedCallback(final Service.State from) {
    String str = String.valueOf(String.valueOf(from));
    return new ListenerCallQueue.Callback<Service.Listener>((new StringBuilder(21 + str.length())).append("terminated({from = ").append(str).append("})").toString()) {
        void call(Service.Listener listener) {
          listener.terminated(from);
        }
      };
  }
  
  private static ListenerCallQueue.Callback<Service.Listener> stoppingCallback(final Service.State from) {
    String str = String.valueOf(String.valueOf(from));
    return new ListenerCallQueue.Callback<Service.Listener>((new StringBuilder(19 + str.length())).append("stopping({from = ").append(str).append("})").toString()) {
        void call(Service.Listener listener) {
          listener.stopping(from);
        }
      };
  }
  
  private final Monitor monitor = new Monitor();
  
  private final Monitor.Guard isStartable = new Monitor.Guard(this.monitor) {
      public boolean isSatisfied() {
        return (AbstractService.this.state() == Service.State.NEW);
      }
    };
  
  private final Monitor.Guard isStoppable = new Monitor.Guard(this.monitor) {
      public boolean isSatisfied() {
        return (AbstractService.this.state().compareTo(Service.State.RUNNING) <= 0);
      }
    };
  
  private final Monitor.Guard hasReachedRunning = new Monitor.Guard(this.monitor) {
      public boolean isSatisfied() {
        return (AbstractService.this.state().compareTo(Service.State.RUNNING) >= 0);
      }
    };
  
  private final Monitor.Guard isStopped = new Monitor.Guard(this.monitor) {
      public boolean isSatisfied() {
        return AbstractService.this.state().isTerminal();
      }
    };
  
  @GuardedBy("monitor")
  private final List<ListenerCallQueue<Service.Listener>> listeners = Collections.synchronizedList(new ArrayList<ListenerCallQueue<Service.Listener>>());
  
  @GuardedBy("monitor")
  private volatile StateSnapshot snapshot = new StateSnapshot(Service.State.NEW);
  
  protected abstract void doStart();
  
  protected abstract void doStop();
  
  public final Service startAsync() {
    if (this.monitor.enterIf(this.isStartable)) {
      try {
        this.snapshot = new StateSnapshot(Service.State.STARTING);
        starting();
        doStart();
      } catch (Throwable startupFailure) {
        notifyFailed(startupFailure);
      } finally {
        this.monitor.leave();
        executeListeners();
      } 
    } else {
      String str = String.valueOf(String.valueOf(this));
      throw new IllegalStateException((new StringBuilder(33 + str.length())).append("Service ").append(str).append(" has already been started").toString());
    } 
    return this;
  }
  
  public final Service stopAsync() {
    if (this.monitor.enterIf(this.isStoppable))
      try {
        String str1, str2;
        Service.State previous = state();
        switch (previous) {
          case NEW:
            this.snapshot = new StateSnapshot(Service.State.TERMINATED);
            terminated(Service.State.NEW);
            break;
          case STARTING:
            this.snapshot = new StateSnapshot(Service.State.STARTING, true, null);
            stopping(Service.State.STARTING);
            break;
          case RUNNING:
            this.snapshot = new StateSnapshot(Service.State.STOPPING);
            stopping(Service.State.RUNNING);
            doStop();
            break;
          case STOPPING:
          case TERMINATED:
          case FAILED:
            str1 = String.valueOf(String.valueOf(previous));
            throw new AssertionError((new StringBuilder(45 + str1.length())).append("isStoppable is incorrectly implemented, saw: ").append(str1).toString());
          default:
            str2 = String.valueOf(String.valueOf(previous));
            throw new AssertionError((new StringBuilder(18 + str2.length())).append("Unexpected state: ").append(str2).toString());
        } 
      } catch (Throwable shutdownFailure) {
        notifyFailed(shutdownFailure);
      } finally {
        this.monitor.leave();
        executeListeners();
      }  
    return this;
  }
  
  public final void awaitRunning() {
    this.monitor.enterWhenUninterruptibly(this.hasReachedRunning);
    try {
      checkCurrentState(Service.State.RUNNING);
    } finally {
      this.monitor.leave();
    } 
  }
  
  public final void awaitRunning(long timeout, TimeUnit unit) throws TimeoutException {
    if (this.monitor.enterWhenUninterruptibly(this.hasReachedRunning, timeout, unit)) {
      try {
        checkCurrentState(Service.State.RUNNING);
      } finally {
        this.monitor.leave();
      } 
    } else {
      String str1 = String.valueOf(String.valueOf(this)), str2 = String.valueOf(String.valueOf(state()));
      throw new TimeoutException((new StringBuilder(66 + str1.length() + str2.length())).append("Timed out waiting for ").append(str1).append(" to reach the RUNNING state. ").append("Current state: ").append(str2).toString());
    } 
  }
  
  public final void awaitTerminated() {
    this.monitor.enterWhenUninterruptibly(this.isStopped);
    try {
      checkCurrentState(Service.State.TERMINATED);
    } finally {
      this.monitor.leave();
    } 
  }
  
  public final void awaitTerminated(long timeout, TimeUnit unit) throws TimeoutException {
    if (this.monitor.enterWhenUninterruptibly(this.isStopped, timeout, unit)) {
      try {
        checkCurrentState(Service.State.TERMINATED);
      } finally {
        this.monitor.leave();
      } 
    } else {
      String str1 = String.valueOf(String.valueOf(this)), str2 = String.valueOf(String.valueOf(state()));
      throw new TimeoutException((new StringBuilder(65 + str1.length() + str2.length())).append("Timed out waiting for ").append(str1).append(" to reach a terminal state. ").append("Current state: ").append(str2).toString());
    } 
  }
  
  @GuardedBy("monitor")
  private void checkCurrentState(Service.State expected) {
    Service.State actual = state();
    if (actual != expected) {
      if (actual == Service.State.FAILED) {
        String str = String.valueOf(String.valueOf(expected));
        throw new IllegalStateException((new StringBuilder(55 + str.length())).append("Expected the service to be ").append(str).append(", but the service has FAILED").toString(), failureCause());
      } 
      String str1 = String.valueOf(String.valueOf(expected)), str2 = String.valueOf(String.valueOf(actual));
      throw new IllegalStateException((new StringBuilder(37 + str1.length() + str2.length())).append("Expected the service to be ").append(str1).append(", but was ").append(str2).toString());
    } 
  }
  
  protected final void notifyStarted() {
    this.monitor.enter();
    try {
      if (this.snapshot.state != Service.State.STARTING) {
        String str = String.valueOf(String.valueOf(this.snapshot.state));
        IllegalStateException failure = new IllegalStateException((new StringBuilder(43 + str.length())).append("Cannot notifyStarted() when the service is ").append(str).toString());
        notifyFailed(failure);
        throw failure;
      } 
      if (this.snapshot.shutdownWhenStartupFinishes) {
        this.snapshot = new StateSnapshot(Service.State.STOPPING);
        doStop();
      } else {
        this.snapshot = new StateSnapshot(Service.State.RUNNING);
        running();
      } 
    } finally {
      this.monitor.leave();
      executeListeners();
    } 
  }
  
  protected final void notifyStopped() {
    this.monitor.enter();
    try {
      Service.State previous = this.snapshot.state;
      if (previous != Service.State.STOPPING && previous != Service.State.RUNNING) {
        String str = String.valueOf(String.valueOf(previous));
        IllegalStateException failure = new IllegalStateException((new StringBuilder(43 + str.length())).append("Cannot notifyStopped() when the service is ").append(str).toString());
        notifyFailed(failure);
        throw failure;
      } 
      this.snapshot = new StateSnapshot(Service.State.TERMINATED);
      terminated(previous);
    } finally {
      this.monitor.leave();
      executeListeners();
    } 
  }
  
  protected final void notifyFailed(Throwable cause) {
    Preconditions.checkNotNull(cause);
    this.monitor.enter();
    try {
      String str1, str2;
      Service.State previous = state();
      switch (previous) {
        case NEW:
        case TERMINATED:
          str1 = String.valueOf(String.valueOf(previous));
          throw new IllegalStateException((new StringBuilder(22 + str1.length())).append("Failed while in state:").append(str1).toString(), cause);
        case STARTING:
        case RUNNING:
        case STOPPING:
          this.snapshot = new StateSnapshot(Service.State.FAILED, false, cause);
          failed(previous, cause);
          break;
        case FAILED:
          break;
        default:
          str2 = String.valueOf(String.valueOf(previous));
          throw new AssertionError((new StringBuilder(18 + str2.length())).append("Unexpected state: ").append(str2).toString());
      } 
    } finally {
      this.monitor.leave();
      executeListeners();
    } 
  }
  
  public final boolean isRunning() {
    return (state() == Service.State.RUNNING);
  }
  
  public final Service.State state() {
    return this.snapshot.externalState();
  }
  
  public final Throwable failureCause() {
    return this.snapshot.failureCause();
  }
  
  public final void addListener(Service.Listener listener, Executor executor) {
    Preconditions.checkNotNull(listener, "listener");
    Preconditions.checkNotNull(executor, "executor");
    this.monitor.enter();
    try {
      if (!state().isTerminal())
        this.listeners.add(new ListenerCallQueue<Service.Listener>(listener, executor)); 
    } finally {
      this.monitor.leave();
    } 
  }
  
  public String toString() {
    String str1 = String.valueOf(String.valueOf(getClass().getSimpleName())), str2 = String.valueOf(String.valueOf(state()));
    return (new StringBuilder(3 + str1.length() + str2.length())).append(str1).append(" [").append(str2).append("]").toString();
  }
  
  private void executeListeners() {
    if (!this.monitor.isOccupiedByCurrentThread())
      for (int i = 0; i < this.listeners.size(); i++)
        ((ListenerCallQueue)this.listeners.get(i)).execute();  
  }
  
  @GuardedBy("monitor")
  private void starting() {
    STARTING_CALLBACK.enqueueOn(this.listeners);
  }
  
  @GuardedBy("monitor")
  private void running() {
    RUNNING_CALLBACK.enqueueOn(this.listeners);
  }
  
  @GuardedBy("monitor")
  private void stopping(Service.State from) {
    if (from == Service.State.STARTING) {
      STOPPING_FROM_STARTING_CALLBACK.enqueueOn(this.listeners);
    } else if (from == Service.State.RUNNING) {
      STOPPING_FROM_RUNNING_CALLBACK.enqueueOn(this.listeners);
    } else {
      throw new AssertionError();
    } 
  }
  
  @GuardedBy("monitor")
  private void terminated(Service.State from) {
    switch (from) {
      case NEW:
        TERMINATED_FROM_NEW_CALLBACK.enqueueOn(this.listeners);
        return;
      case RUNNING:
        TERMINATED_FROM_RUNNING_CALLBACK.enqueueOn(this.listeners);
        return;
      case STOPPING:
        TERMINATED_FROM_STOPPING_CALLBACK.enqueueOn(this.listeners);
        return;
    } 
    throw new AssertionError();
  }
  
  @GuardedBy("monitor")
  private void failed(final Service.State from, final Throwable cause) {
    String str1 = String.valueOf(String.valueOf(from)), str2 = String.valueOf(String.valueOf(cause));
    (new ListenerCallQueue.Callback<Service.Listener>((new StringBuilder(27 + str1.length() + str2.length())).append("failed({from = ").append(str1).append(", cause = ").append(str2).append("})").toString()) {
        void call(Service.Listener listener) {
          listener.failed(from, cause);
        }
      }).enqueueOn(this.listeners);
  }
  
  @Immutable
  private static final class StateSnapshot {
    final Service.State state;
    
    final boolean shutdownWhenStartupFinishes;
    
    @Nullable
    final Throwable failure;
    
    StateSnapshot(Service.State internalState) {
      this(internalState, false, null);
    }
    
    StateSnapshot(Service.State internalState, boolean shutdownWhenStartupFinishes, @Nullable Throwable failure) {
      Preconditions.checkArgument((!shutdownWhenStartupFinishes || internalState == Service.State.STARTING), "shudownWhenStartupFinishes can only be set if state is STARTING. Got %s instead.", new Object[] { internalState });
      Preconditions.checkArgument(((((failure != null) ? 1 : 0) ^ ((internalState == Service.State.FAILED) ? 1 : 0)) == 0), "A failure cause should be set if and only if the state is failed.  Got %s and %s instead.", new Object[] { internalState, failure });
      this.state = internalState;
      this.shutdownWhenStartupFinishes = shutdownWhenStartupFinishes;
      this.failure = failure;
    }
    
    Service.State externalState() {
      if (this.shutdownWhenStartupFinishes && this.state == Service.State.STARTING)
        return Service.State.STOPPING; 
      return this.state;
    }
    
    Throwable failureCause() {
      Preconditions.checkState((this.state == Service.State.FAILED), "failureCause() is only valid if the service has failed, service is %s", new Object[] { this.state });
      return this.failure;
    }
  }
}

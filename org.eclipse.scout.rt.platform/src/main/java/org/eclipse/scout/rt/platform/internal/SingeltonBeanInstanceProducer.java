/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.internal;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanInstanceProducer;
import org.eclipse.scout.rt.platform.exception.BeanCreationException;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This producers creates a new instance for the given bean or returns the already created instance. It is used as
 * default producer for {@link ApplicationScoped} beans. Further, it does not only return always the same instance but
 * ensures also that at most one instance is created and initialized (a bean that throws an exception during its
 * instantiation is not considered instantiated).
 * <p>
 * Behavior of {@link NonSingeltonBeanInstanceProducer} applies also on this producer (initialization, circular
 * dependencies check, ...)
 * <p>
 * This class is thread safe. In case of concurrent invocations of {@link #produce(IBean)} at most one thread creates
 * the bean. Any other thread requesting the application-scoped instance is suspended until the creator thread completes
 * or after waiting at most 90 seconds. Potential deadlocks are reported in the log and a {@link BeanCreationException}
 * is thrown. Potential dead locks are logged after 5 seconds on level <code>WARN</code>, if the class' log level is
 * <code>DEBUG</code> (speeds up identifying potential deadlocks during development) and another one is logged after
 * waiting 90 seconds if the log level is <code>WARN</code>.
 * <p>
 * <b>Important:</b> If the bean's initialization depends on (external) resources which might not be available at the
 * time of construction and the initialization process waits until they are available, they should not be initialized in
 * the constructor or in a {@link PostConstruct} annotated method. Other threads requesting the same bean would run into
 * the potential deadlock detection and a {@link BeanCreationException} would be thrown. These resources should be
 * lazily initialized using a synchronized "ensure-initialized" pattern.
 * <p>
 * <b>Ensure-initialized example:</b> A possible base implementation of the "ensure-initialized" pattern:
 *
 * <pre>
 * &#64;ApplicationScoped
 * public abstract class AbstractEnsureInitializedBean&lt;T&gt; {
 *
 *   private final FinalValue&lt;T&gt; m_resource = new FinalValue&lt;&gt;();
 *
 *   protected abstract T lazyInitResource() throws Exception;
 *
 *   protected T getResource() {
 *     if (!m_resource.isSet()) {
 *       synchronized (m_resource) {
 *         m_resource.setIfAbsent(new Callable&lt;T&gt;() {
 *           &#64;Override
 *           public T call() throws Exception {
 *             return lazyInitResource();
 *           }
 *         });
 *       }
 *     }
 *     return m_resource.get();
 *   }
 * }
 * </pre>
 *
 * @see NonSingeltonBeanInstanceProducer
 */
public class SingeltonBeanInstanceProducer<T> implements IBeanInstanceProducer<T> {

  private static final Logger LOG = LoggerFactory.getLogger(SingeltonBeanInstanceProducer.class);

  /**
   * Default max wait time in seconds another thread will wait on the creator thread which is instantiating an
   * application-scoped bean.
   */
  private static final int DEADLOCK_DETECTION_MAX_WAIT_TIME_SECONDS = 90;

  /** Time in seconds another thread will wait before a log entry is created with level DEBUG. */
  private static final int DEADLOCK_DETECTION_DEBUG_WAIT_TIME_SECONDS = 5;

  private final FinalValue<T> m_instance = new FinalValue<>();
  private final AtomicReference<Thread> m_creatorThread = new AtomicReference<>();

  @Override
  public T produce(IBean<T> bean) {
    return BeanInstanceUtil.createAndAssertNoCircularDependency(() -> getOrCreateInstance(bean), bean.getBeanClazz());
  }

  protected T getOrCreateInstance(final IBean<T> bean) {
    T instance = m_instance.get();
    if (instance != null) {
      return instance;
    }

    if (m_creatorThread.compareAndSet(null, Thread.currentThread())) {
      try {
        // check again to avoid race conditions
        instance = m_instance.get();
        if (instance != null) {
          return instance;
        }
        // current thread has to create instance
        instance = createInstance(bean);
        m_instance.set(instance);
        return instance;
      }
      finally {
        synchronized (this) {
          // reset creator thread so that another one tries to create the bean again in case the current ran into an exception.
          m_creatorThread.set(null);
          // wake up other threads waiting on the application-scoped instance
          this.notifyAll();
        }
      }
    }

    // remember creator thread for logging purposes
    final Thread creatorThread = m_creatorThread.get();
    final int maxWaitTimeSeconds = getDeadlockDetectionMaxWaitTimeSeconds();
    final long maxWaitEndTimeMillis = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(maxWaitTimeSeconds);
    boolean logDebug = LOG.isDebugEnabled();
    do {
      try {
        synchronized (this) {
          if (m_creatorThread.get() == null) {
            break;
          }
          long waitTimeMillis = logDebug
              ? TimeUnit.SECONDS.toMillis(Math.min(maxWaitTimeSeconds, DEADLOCK_DETECTION_DEBUG_WAIT_TIME_SECONDS))
              : maxWaitEndTimeMillis - System.currentTimeMillis();
          if (waitTimeMillis > 0) {
            // wait for the creator to complete, but not too long because the notify signal could have been missed
            this.wait(waitTimeMillis);
          }
        }
      }
      catch (InterruptedException e) {
        throw new ThreadInterruptedError("Thread has been interrupted");
      }
      if (m_creatorThread.get() == null) {
        break;
      }
      if (logDebug) {
        logWarnPotentialDeadlock(creatorThread);
        logDebug = false;
      }
    }
    while (System.currentTimeMillis() < maxWaitEndTimeMillis); // try as long as the other thread is still creating the bean and the max wait time has not been elapsed

    // check if bean has been created in the meantime
    instance = m_instance.get();
    if (instance != null) {
      return instance;
    }

    // bean has not been created
    if (System.currentTimeMillis() < maxWaitEndTimeMillis) {
      throw new BeanCreationException("Thread was waiting on bean instance creator thread which most likely failed (check the log).")
          .withContextInfo("beanClass", bean == null || bean.getBeanClazz() == null ? "n/a" : bean.getBeanClazz().getName())
          .withContextInfo("creatorThreadID", creatorThread == null ? "n/a" : creatorThread.getId())
          .withContextInfo("creatorThreadName", creatorThread == null ? "n/a" : creatorThread.getName());
    }
    else {
      logWarnPotentialDeadlock(creatorThread);
      throw new BeanCreationException("Potential deadlock detected: bean is being created by another thread. Either the creation takes longer than {}s "
          + "or the current and the creator threads are blocking each other (check the log).", maxWaitTimeSeconds)
              .withContextInfo("beanClass", bean == null || bean.getBeanClazz() == null ? "n/a" : bean.getBeanClazz().getName())
              .withContextInfo("creatorThreadID", creatorThread == null ? "n/a" : creatorThread.getId())
              .withContextInfo("creatorThreadName", creatorThread == null ? "n/a" : creatorThread.getName());
    }
  }

  /**
   * Logs the potential deadlock in which the current and the given creator threads are involved in.
   */
  private void logWarnPotentialDeadlock(final Thread creatorThread) {
    if (!LOG.isWarnEnabled()) {
      return;
    }

    final Thread current = Thread.currentThread();
    StringBuilder threadInfos = new StringBuilder();
    threadInfos.append("creator Thread: ");
    if (creatorThread == null) {
      threadInfos.append(" n/a");
    }
    else {
      threadInfos.append(creatorThread.getId()).append(" - ").append(creatorThread.getName());
      for (StackTraceElement traceElement : creatorThread.getStackTrace()) {
        threadInfos.append("\n\tat ");
        threadInfos.append(traceElement);
      }
    }
    threadInfos.append("\ncurrent Thread: ");
    threadInfos.append(current.getId()).append(" - ").append(current.getName());
    int stackElement = -1;
    for (StackTraceElement traceElement : current.getStackTrace()) {
      stackElement++;
      if (stackElement < 2) {
        // ignore the first two method calls
        continue;
      }
      threadInfos.append("\n\tat ");
      threadInfos.append(traceElement);
    }

    LOG.warn("potential deadlock detected\n{}\n", threadInfos);
  }

  /**
   * Max wait time in seconds another thread will wait on the creator thread which is instantiating an
   * application-scoped bean. This default implementation returns 90s.
   *
   * @return Returns a positive integer value. The value should be grater than 1 second.
   */
  protected int getDeadlockDetectionMaxWaitTimeSeconds() {
    return DEADLOCK_DETECTION_MAX_WAIT_TIME_SECONDS;
  }

  /**
   * Creates a new instance for a bean. May be called more than once per bean class even for application scoped beans.
   *
   * @return new instance. Never <code>null</code>.
   */
  protected T createInstance(IBean<T> bean) {
    T instance = BeanInstanceUtil.createBean(bean.getBeanClazz());
    BeanInstanceUtil.initializeBeanInstance(instance);
    return instance;
  }
}

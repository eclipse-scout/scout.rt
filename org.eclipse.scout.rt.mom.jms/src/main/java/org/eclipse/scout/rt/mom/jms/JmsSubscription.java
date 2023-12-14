/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.jms;

import java.util.concurrent.TimeUnit;

import jakarta.jms.MessageConsumer;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IMessageListener;
import org.eclipse.scout.rt.mom.api.IMom;
import org.eclipse.scout.rt.mom.api.IRequestListener;
import org.eclipse.scout.rt.mom.api.ISubscription;
import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.mom.jms.internal.ISubscriptionStats;
import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * Represents a {@link MessageConsumer} in the JMS messaging standard.
 *
 * @see IMom
 * @since 6.1
 */
public class JmsSubscription implements ISubscription {

  protected final IDestination<?> m_destination;
  protected final IMessageListener<?> m_messageListener;
  protected final IRequestListener<?, ?> m_requestListener;
  protected final SubscribeInput m_subscribeInput;
  protected final IJmsSessionProvider m_sessionProvider;
  protected final IFuture<?> m_jobMonitor;

  public JmsSubscription(IDestination<?> destination, IMessageListener<?> messageListener, IRequestListener<?, ?> requestListener, SubscribeInput subscribeInput, IJmsSessionProvider sessionProvider, IFuture<?> jobMonitor) {
    m_destination = destination;
    m_messageListener = messageListener;
    m_requestListener = requestListener;
    m_subscribeInput = subscribeInput;
    m_sessionProvider = sessionProvider;
    m_jobMonitor = jobMonitor;
  }

  @Override
  public IDestination<?> getDestination() {
    return m_destination;
  }

  @Override
  public IMessageListener<?> getMessageListener() {
    return m_messageListener;
  }

  @Override
  public IRequestListener<?, ?> getRequestListener() {
    return m_requestListener;
  }

  @Override
  public SubscribeInput getSubscribeInput() {
    return m_subscribeInput;
  }

  @Override
  public void dispose() {
    m_sessionProvider.close();
    if (SubscribeInput.ACKNOWLEDGE_AUTO_SINGLE_THREADED == m_subscribeInput.getAcknowledgementMode()) {
      // Close did not throw an exception
      // In case of single threaded subscription we wait for the job to finish
      // This allows API clients to wait for any ongoing message processing
      m_jobMonitor.awaitDone();
    }
  }

  @Override
  public boolean isDisposed() {
    return m_sessionProvider.isClosing();
  }

  @Override
  public ISubscriptionStats getStats() {
    return m_sessionProvider.getStats();
  }

  /**
   * Wait until the subscription has really started consuming incoming messages.
   * <p>
   * This is a best effort approach. This method returns immediately if
   * <ul>
   * <li>the call to {@link #getStats()} returns null</li>
   * <li>the listening job {@link IFuture#isFinished()}</li>
   * <li>the current thread {@link Thread#isInterrupted()}</li>
   *
   * @param time
   *          the intended wait time, value of {@link JmsMomImplementor#WAIT_TIME_INFINITE} may be used to wait
   *          infinitely
   * @since 6.1
   */
  public boolean awaitStarted(int time, TimeUnit unit) {
    long timeoutNanos = System.nanoTime() + unit.toNanos(time);
    while (true) {
      if (Thread.currentThread().isInterrupted()) {
        return false;
      }
      if (time != JmsMomImplementor.WAIT_TIME_INFINITE && System.nanoTime() >= timeoutNanos) {
        return false;
      }
      if (m_jobMonitor.isFinished()) {
        return false;
      }
      ISubscriptionStats stats = getStats();
      if (stats != null && (stats.invokingReceive() || stats.receivedMessages() > 0 || stats.receivedErrors() > 0)) {
        return true;
      }
      Thread.yield();
    }
  }

}

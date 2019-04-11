/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.jms;

import java.util.concurrent.TimeUnit;

import javax.jms.MessageConsumer;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IMom;
import org.eclipse.scout.rt.mom.api.ISubscription;
import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.mom.jms.internal.IJmsSessionProvider2;
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
  protected final SubscribeInput m_subscribeInput;
  protected final IJmsSessionProvider m_sessionProvider;
  protected final IFuture<?> m_jobMonitor;

  public JmsSubscription(IDestination<?> destination, SubscribeInput subscribeInput, IJmsSessionProvider sessionProvider, IFuture<?> jobMonitor) {
    m_destination = destination;
    m_subscribeInput = subscribeInput;
    m_sessionProvider = sessionProvider;
    m_jobMonitor = jobMonitor;
  }

  @Override
  public IDestination<?> getDestination() {
    return m_destination;
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

  /**
   * @return the stats of this subscription since the real jms session was started. Returns null if the jms connection
   *         is currently down.
   * @since 6.1
   */
  public ISubscriptionStats getStats() {
    if (m_sessionProvider instanceof IJmsSessionProvider2) {
      return ((IJmsSessionProvider2) m_sessionProvider).getStats();
    }
    return null;
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
   * @since 6.1
   */
  public boolean awaitStarted(int time, TimeUnit unit) {
    if (!(m_sessionProvider instanceof IJmsSessionProvider2)) {
      return false;
    }
    long timeoutNanos = System.nanoTime() + unit.toNanos(time);
    while (true) {
      if (Thread.currentThread().isInterrupted()) {
        return false;
      }
      if (System.nanoTime() >= timeoutNanos) {
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

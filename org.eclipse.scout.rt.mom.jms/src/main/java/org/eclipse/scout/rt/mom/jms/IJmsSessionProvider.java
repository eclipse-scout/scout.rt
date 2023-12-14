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

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TemporaryQueue;

import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.mom.jms.internal.ISubscriptionStats;

/**
 * This provider is used as an abstraction to allow creation and destruction of session and if required also
 * connections.
 * <p>
 * {@link Session} is not thread safe. The only method allowed to be called from other threads is
 * {@link Session#close()}. This interface has the same behavior: Objects implementing this interface are not thread
 * safe but {@link #close()} and {@link #isClosing()} can be called from other threads.
 *
 * @since 6.1
 */
public interface IJmsSessionProvider {

  /**
   * @return JMS {@link Session}
   *         <p>
   *         Do not cache or keep references to this value, it may change during failover
   */
  Session getSession() throws JMSException;

  /**
   * Get or creates a new {@link MessageProducer}. If a new {@link MessageProducer} is created, {@link JMSException} may
   * be thrown.
   * <p>
   * Do not cache or keep references to this value, it may change during failover
   *
   * @return lazy created JMS {@link MessageProducer}
   * @throws JMSException
   *           if the session fails to create a {@link MessageProducer} due to some internal error.
   */
  MessageProducer getProducer() throws JMSException;

  /**
   * Get or creates a new {@link MessageConsumer}. If a new {@link MessageConsumer} is created, {@link JMSException} may
   * be thrown.
   * <p>
   * Do not cache or keep references to this value, it may change during failover
   *
   * @return lazy created JMS {@link MessageConsumer}
   * @throws JMSException
   *           if the session fails to create a {@link MessageConsumer} due to some internal error.
   */
  MessageConsumer getConsumer(SubscribeInput input) throws JMSException;

  /**
   * Get or creates a new {@link TemporaryQueue}. If a new {@link TemporaryQueue} is created, {@link JMSException} may
   * be thrown.
   * <p>
   * Do not cache or keep references to this value, it may change during failover
   *
   * @return lazy created JMS {@link TemporaryQueue}
   * @throws JMSException
   *           if the session fails to create a {@link TemporaryQueue} due to some internal error.
   */
  TemporaryQueue getTemporaryQueue() throws JMSException;

  /**
   * May be used to cleanup created temporary queues. This method can be only called on a closed session provider.
   * <p>
   * After closing the session, the no more required temporary queue is deleted. The queue is associated with the
   * connection and not the session and exists as long as the connection is alive.
   *
   * @throws JMSException
   *           If there are existing receivers still using it or if the JMS provider fails to delete the temporary queue
   *           due to some internal error.
   */
  void deleteTemporaryQueue() throws JMSException;

  /**
   * May be called from threads not owing this object.
   *
   * @return true if {@link #close()} was called (in any thread)
   */
  boolean isClosing();

  /**
   * Closes any JMS resources that were acquired.
   * <p>
   * May be called from threads not owing this object.
   */
  void close();

  /**
   * @param receiveTimeoutMillis
   *          0 for no timeout
   * @return the next message or null if the consumer has no more messages
   * @throws JMSException
   *           if the jms connection was closed and failover was not possible
   * @since 6.1 moved to this interface in 10.0
   */
  Message receive(SubscribeInput subscribeInput, long receiveTimeoutMillis) throws JMSException;

  /**
   * @return subscription statistics since the last (re-)connect
   *         <p>
   *         Used in {@link JmsSubscription#awaitStarted(int, java.util.concurrent.TimeUnit)} and for unit testing
   * @since 6.1 moved to this interface in 10.0
   */
  ISubscriptionStats getStats();
}

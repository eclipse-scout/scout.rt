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
package org.eclipse.scout.rt.mom.jms.internal;

import javax.jms.JMSException;
import javax.jms.Message;

import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.mom.jms.IJmsSessionProvider;
import org.eclipse.scout.rt.mom.jms.JmsSubscription;

/**
 * Additional methods to {@link IJmsSessionProvider}. This interface is used in order not to break the existing API.
 *
 * @since 6.1
 */
public interface IJmsSessionProviderEx extends IJmsSessionProvider {
  /**
   * @param subscribeInput
   * @return the next message or null if the consumer has no more messages
   * @throws JMSException
   *           if the jms connection was closed and failover was not possible
   * @since 6.1
   */
  Message receive(SubscribeInput subscribeInput) throws JMSException;

  /**
   * @return subscription statistics since the last (re-)connect
   *         <p>
   *         Used in {@link JmsSubscription#awaitStarted(int, java.util.concurrent.TimeUnit)} and for unit testing
   * @since 6.1
   */
  ISubscriptionStats getStats();
}

/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.mom.jms.internal;

import org.eclipse.scout.rt.mom.jms.JmsSubscription;

/**
 * Helper used in {@link JmsSubscription#awaitStarted(int, java.util.concurrent.TimeUnit)} and unit testing
 *
 * @since 6.1
 */
public interface ISubscriptionStats {

  /**
   * @return true if the subscription is just before or inside a call to MessageConsumer#receive
   * @since 6.1
   */
  boolean invokingReceive();

  /**
   * @return the number of messages recevied, including null messages
   * @since 6.1
   */
  long receivedMessages();

  /**
   * @return the number of non-null messages recevied
   * @since 6.1
   */
  long receivedNonNullMessages();

  /**
   * @return the number of message errors occured upon calling receive()
   */
  long receivedErrors();
}
